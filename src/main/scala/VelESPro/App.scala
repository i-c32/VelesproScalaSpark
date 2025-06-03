package VelESPro

import Parameters.Par
import Parameters.Par._
import org.apache.spark.sql.{DataFrame, SparkSession}
import org.apache.spark.sql.functions.lit

import java.time.LocalDateTime

/**
 * @author ${Ivan}
 */
object App {

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
    if (args.length < 1) {
      println("Please provide the input argument.")
      System.exit(1)
    }
    run(args(0), args(1))
    spark.stop()
  }

}
