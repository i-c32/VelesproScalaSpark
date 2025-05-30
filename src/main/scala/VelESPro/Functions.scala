package VelESPro

import Parameters.Par
import VelESPro.App.spark
import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.functions._
import spark.implicits._

import scala.annotation.tailrec
import scala.collection.mutable.ListBuffer

class MOp {
  def sumMatrix(A: Array[Array[Double]], B: Array[Array[Double]]): Array[Array[Double]] = {
    val sumM: Array[Array[Double]] = Array.ofDim[Double](A.length, 3)
    for (i <- A.indices) {
      sumM(i) = A(i).zip(B(i)).map(x => x._1 + x._2)
    }
    sumM
  }

  def susMatrix(A: Array[Array[Double]], B: Array[Array[Double]]): Array[Array[Double]] = {
    val susM: Array[Array[Double]] = Array.ofDim[Double](A.length, 3)
    for (i <- A.indices) {
      susM(i) = A(i).zip(B(i)).map(x => x._1 - x._2)
    }
    susM
  }

  def compMatProd(df1: DataFrame,
                  df2: DataFrame,
                  nMatL: Seq[Int],
                  nMatTot: Int
                 ): DataFrame = {

    val results = ListBuffer.fill(nMatTot)(0.0)
    var nn = 0

    for (row <- nMatL; coll <- nMatL) {
      val rowL = df1
        .filter(col("Row") === row)
        .orderBy("Column")
        .select("V1")
        .withColumn("index", monotonically_increasing_id())

      val colL = df2
        .filter(col("Column") === coll)
        .orderBy("Row")
        .select("V2")
        .withColumn("index", monotonically_increasing_id())

      val productSum = rowL
        .join(colL, Seq("index"))
        .withColumn("mult", col("V1") * col("V2"))
        .agg(sum("mult"))
        .first()(0)
        .asInstanceOf[Double]

      results(nn) = productSum
      nn += 1
    }

    results.zipWithIndex.toSeq.toDF("Result", "index")
  }

  @tailrec
  final def diag(matt: DataFrame, iteration: Int = 0, maxIter: Int = 3): DataFrame = {
    if (iteration >= maxIter) {
      println(s"Reached maximum iterations ($maxIter). Returning last matrix.")
      return matt
    }

    val nMatTot = matt.count().toInt
    val nMat = math.sqrt(nMatTot).toInt

    // elementos fuera de la diagonal de la matriz
    val offDim = matt.filter(col("Row") =!= col("Column"))
    val offDim0 = offDim.filter(abs(col("Value")) > Par.errDiag)
    if (offDim0.isEmpty) {
      //Define matrix size
      matt
    } else {
      val cot = "cos(t)"
      val sit = "sin(t)"
      // Find the maximum value in the "Value" column
      val maxRow = offDim0.agg(max("Value").as("Value"), min("Row").as("Row"), first("Column").as("Column"), first("Name").as("Name"))

      // Get the Row and Column values from the max value
      val rowValue = maxRow.select("Row").first()(0)
      val colValue = maxRow.select("Column").first()(0)

      // Columnas para el valor del angulo
      val rowEqualsRow = col("Row") === rowValue && col("Column") === rowValue
      val colEqualsCol = col("Row") === colValue && col("Column") === colValue
      val rowEqualsCol = col("Row") === rowValue && col("Column") === colValue

      val valAng = matt.filter(rowEqualsRow || colEqualsCol || rowEqualsCol)

      val valAng1 = valAng.orderBy(col("Row").asc, col("Column").asc).
        withColumn("index", monotonically_increasing_id()).
        withColumn("nam",
          when(col("index") === 0, lit("Aii")).
            when(col("index") === 1, lit("Aij")).
            when(col("index") === 2, lit("Ajj"))
        )

      val valAng2 = valAng1.groupBy(col("Name")).pivot(col("nam")).agg(first("Value"))
      // Se calcula el coseno y el seno.
      val valAng3 = valAng2.withColumn("beta", (col("Ajj") - col("Aii")) / (col("Aij") * 2))
        .withColumn("sqrt_t", lit(1.0) + col("beta") * col("beta"))
        .withColumn("t", sign(col("beta")) / (abs(col("beta")) + sqrt(col("sqrt_t"))))
        .withColumn(cot, lit(1.0) / sqrt(lit(1.0) + col("t") * col("t")))
        .withColumn(sit, col(cot )* col("t"))

      // Se obtiene el valor del coseno y del seno
      val ct = valAng3.select(cot).first()(0).asInstanceOf[Double]
      val st = valAng3.select(sit).first()(0).asInstanceOf[Double]

      //Define matrix size
      //Se tiene que transformar en integer
      val rowVI = rowValue.toString.toInt
      val colVI = colValue.toString.toInt
      // Generate Identity Matrix as a flattened list
      val identityMatrixValues = Array.tabulate(nMat, nMat) { (i, j) =>
        val isIRowVI = i == rowVI
        val isIColVI = i == colVI
        val isJRowVI = j == rowVI
        val isJColVI = j == colVI

        (isIRowVI, isIColVI, isJRowVI, isJColVI) match {
          case (true, _, true, _)  => ct         // (i == rowVI && j == rowVI)
          case (_, true, _, true)  => ct         // (i == colVI && j == colVI)
          case (true, _, _, true)  => st         // (i == rowVI && j == colVI)
          case (_, true, true, _)  => -st        // (i == colVI && j == rowVI)
          case (_, _, _, _) if i == j => 1.0     // identity diagonal
          case _ => 0.0
        }
      }.flatten
      // Convert values into a DataFrame
      val identityDF = identityMatrixValues.zipWithIndex.toSeq.toDF("DiagM", "index")

      val matt1 = matt.drop("DiagM")
      val matt2 = matt1.join(identityDF, Seq("index"))

      val nMatL = (0 until nMat).toList

      val dff1 = matt2.select(col("Row"),col("Column"),col("Value").alias("V1"))
      val dff2 = matt2.select(col("Row"),col("Column"),col("DiagM").alias("V2"))
      val dfMP = compMatProd(dff1, dff2,
        nMatL: Seq[Int],
        nMatTot: Int
      )
      val matt3 = matt2.join(dfMP.withColumnRenamed("Result", "VDMT"), "index")

      // Se intercambian filas y columnas para que sea la transpuesta
      val dff3 = matt3.select(col("Row").alias("Column"),col("Column").alias("Row"),col("DiagM").alias("V1"))
      val dff4 = matt3.select(col("Row"),col("Column"),col("VDMT").alias("V2"))
      val dfMP1 = compMatProd(dff3, dff4,
        nMatL: Seq[Int],
        nMatTot: Int
      )

      val matt4 = matt3.join(dfMP1.withColumnRenamed("Result", "F"), "index")

      val matt5 = matt4.drop("VDMT","Value").withColumnRenamed("F","Value")
      diag(matt5, iteration + 1, maxIter)
    }
  }
}

class Errores {
  // Calculo del error de una matriz
  def errorM(A: Array[Array[Double]], B: Array[Array[Double]]): Double = {

    val numEle = A.length*A(0).length// numero de elementos
    val mOp = new MOp()
    val MEr = mOp.susMatrix(A,B)
    val MError = MEr.flatten.map(_.abs).sum/numEle
    MError
  }
}