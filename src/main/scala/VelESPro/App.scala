package VelESPro

import Parameters.Par
import org.apache.logging.log4j.{LogManager, Logger}
import org.apache.spark.sql.{DataFrame, SparkSession}
import org.apache.spark.sql.functions.lit

import java.time.LocalDateTime

/**
 * @author ${Ivan}
 */
object App {

  val logger: Logger = LogManager.getLogger(this.getClass)

  val spark: SparkSession = SparkSession
    .builder()
    .master("local[*]")
    .appName("VelESPro")
    .getOrCreate()

  def run(
           inOpF: String,
           outputF: String,
           createMolecule: String => Molecule = new Molecule(_),
           createSimetria: (DataFrame, DataFrame) => Simetria = new Simetria(_, _)
         ): Unit = {
    // Tiempo de inicio
    val currentDateTime = LocalDateTime.now()

    // Se lee la molecula y se guardan los datos en dos dataframes
    val rMol = createMolecule(inOpF)

    // Se obtiene la simetria de la molecula
    val sim = createSimetria(rMol.cAtF, rMol.molF)

    val molF1 = sim.molF.withColumn(Par.cTime, lit(currentDateTime))

    //Se imprime los resultados como parquet
    molF1.coalesce(1).write.mode("overwrite").parquet(outputF)
    rMol.cAtF.write.mode("append").parquet(outputF)
  }

  def main(args: Array[String]): Unit = {
    if (args.length < 2) {
      logger.error("Provide the input or the output")
      System.exit(1)
    }

    val inputPath = args(0)
    val outputPath = args(1)

    try {
      run(inputPath, outputPath)
    } catch {
      case ex: Exception =>
        logger.error("Application failed", ex)
        System.exit(2)
    } finally {
      spark.stop() // Ensure it's defined properly
    }

  }

}
