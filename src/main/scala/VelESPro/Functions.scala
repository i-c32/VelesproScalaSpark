package VelESPro

import Parameters.Par
import VelESPro.App.spark
import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.functions._
import spark.implicits._

import scala.annotation.tailrec
import org.apache.logging.log4j.{LogManager,Logger}

class MOp {

  val logger: Logger = LogManager.getLogger(this.getClass)

  def compMatProd(df1: DataFrame,
                  df2: DataFrame,
                 ): DataFrame = {

    logger.info("Start multiplication")

    val result = df1.join(df2,$"C1" === $"R2").
      withColumn("Product", $"V1"*$"V2").
      groupBy($"R1",$"C2").
      agg(sum("Product").as("Result")).
      orderBy("R1","C2").
      withColumn("index", monotonically_increasing_id())

    logger.info("End multiplication")
    result.select("Result", "index")

  }

  @tailrec
  final def diag(matt: DataFrame, nMatTot: Int, colID: String, iteration: Int = 0, maxIter: Int = 10): DataFrame = {
    if (iteration >= maxIter) {
      println(s"Reached maximum iterations ($maxIter). Returning last matrix.")
      return matt
    }

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
      val maxRow = offDim0.agg(max("Value").as("Value"),
          min("Row").as("Row"),
          first("Column").as("Column"),
          first(colID).as(colID))
        .collect()(0)

      // Get the Row and Column values from the max value
      val rowValue = maxRow.getAs[String]("Row").toInt
      val colValue = maxRow.getAs[String]("Column").toInt

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

      val valAng2 = valAng1.groupBy(col(colID)).pivot(col("nam")).agg(first("Value"))
      // Se calcula el coseno y el seno.
      val valAng3 = valAng2.withColumn("beta", (col("Ajj") - col("Aii")) / (col("Aij") * 2))
        .withColumn("sqrt_t", lit(1.0) + col("beta") * col("beta"))
        .withColumn("t", sign(col("beta")) / (abs(col("beta")) + sqrt(col("sqrt_t"))))
        .withColumn(cot, lit(1.0) / sqrt(lit(1.0) + col("t") * col("t")))
        .withColumn(sit, col(cot )* col("t")).collect()(0)

      // Se obtiene el valor del coseno y del seno
      val ct = valAng3.getAs[Double](cot)
      val st = valAng3.getAs[Double](sit)

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

      val dff1 = matt2.select(col("Row").alias("R1"),col("Column").alias("C1"),col("Value").alias("V1"))
      val dff2 = matt2.select(col("Row").alias("R2"),col("Column").alias("C2"),col("DiagM").alias("V2"))
      val dfMP = compMatProd(dff1, dff2)
      val matt3 = matt2.join(dfMP.withColumnRenamed("Result", "VDMT"), "index")

      // Se intercambian filas y columnas para que sea la transpuesta
      val dff3 = matt3.select(col("Row").alias("C1"),col("Column").alias("R1"),col("DiagM").alias("V1"))
      val dff4 = matt3.select(col("Row").alias("R2"),col("Column").alias("C2"),col("VDMT").alias("V2"))
      val dfMP1 = compMatProd(dff3, dff4)

      val matt4 = matt3.join(dfMP1.withColumnRenamed("Result", "F"), "index")

      val matt5 = matt4.drop("VDMT","Value").withColumnRenamed("F","Value")
      diag(matt5, nMatTot, colID, iteration + 1, maxIter)
    }
  }
}