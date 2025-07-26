package VelESPro

import Parameters.Par
import VelESPro.App.spark
import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.functions._
import spark.implicits._
import scala.math.{signum, abs => mabs, sqrt => msqrt}

import scala.annotation.tailrec
import org.apache.logging.log4j.{LogManager,Logger}

class MOp {

  val logger: Logger = LogManager.getLogger(this.getClass)

  def compMatProd(df1: DataFrame,
                  df2: DataFrame,
                 ): DataFrame = {

    val result = df1.join(df2,$"C1" === $"R2").
      withColumn("Product", $"V1"*$"V2").
      groupBy($"R1",$"C2").
      agg(sum("Product").as("Result")).
      orderBy("R1","C2").
      withColumn("index", monotonically_increasing_id())

    result.select("Result", "index")

  }

  @tailrec
  final def diag(matt: DataFrame, nMatTot: Int, colID: String, iteration: Int = 0, maxIter: Int = 10): DataFrame = {
    if (iteration >= maxIter) {
      logger.warn(s"Reached maximum iterations ($maxIter). Returning last matrix.")
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
      logger.info("Obtain the sin and cos")
      val row = valAng2.select(
        (col("Ajj") - col("Aii")) / (col("Aij") * 2) as "beta"
      ).first()

      val beta = row.getAs[Double]("beta")
      val sqrtT = 1.0 + beta * beta
      val t = signum(beta) / (mabs(beta) + msqrt(sqrtT))
      val ct = 1.0 / msqrt(1.0 + t * t)
      val st = ct * t

      // Generate Identity Matrix as a flattened list
      val identityMatrixValues = Array.tabulate(nMat, nMat) { (i, j) =>
        (i, j) match {
          case (`rowValue`, `rowValue`) => ct
          case (`colValue`, `colValue`) => ct
          case (`rowValue`, `colValue`) => st
          case (`colValue`, `rowValue`) => -st
          case _ if i == j        => 1.0
          case _                  => 0.0
        }
      }.flatten
      logger.info(s"Matrix Multiplication, iteration: $iteration")
      // Create identity DataFrame with index
      val identityDF = identityMatrixValues.zipWithIndex.toSeq.toDF("DiagM", "index")

      val matt1 = matt.drop("DiagM").join(identityDF, Seq("index"))

      logger.info(s"First Matrix Multiplication")
      val dfMP = compMatProd(
        matt1.select(col("Row").alias("R1"),col("Column").alias("C1"),col("Value").alias("V1")),
        matt1.select(col("Row").alias("R2"),col("Column").alias("C2"),col("DiagM").alias("V2"))
      )
      logger.info(s"First join")
      val matt3 = matt1.join(dfMP.withColumnRenamed("Result", "VDMT"), "index")

      // Se intercambian filas y columnas para que sea la transpuesta
      logger.info(s"Second Matrix Multiplication")
      val dfMP1 = compMatProd(
        matt3.select(col("Row").alias("C1"),col("Column").alias("R1"),col("DiagM").alias("V1")),
        matt3.select(col("Row").alias("R2"),col("Column").alias("C2"),col("VDMT").alias("V2"))
      )

      logger.info(s"Second join")
      val matt4 = matt3.join(dfMP1.withColumnRenamed("Result", "F"), "index").
        drop("VDMT","Value").
        withColumnRenamed("F","Value")

      diag(matt4, nMatTot, colID, iteration + 1, maxIter)
    }
  }
}