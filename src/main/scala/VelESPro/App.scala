package VelESPro

import Parameters.Par
import Parameters.Par._
import org.apache.spark.sql.SparkSession
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

  def main(args : Array[String]): Unit = {

    // Declare the variable before the if-else block to ensure it's in scope
    var inOpF: String = ""

    // Se lee el nombre del fichero de entrada.
    if (args.length < 1) {
      println("Please provide the input argument.")
      System.exit(1) // Exit if argument is missing
    } else {
      inOpF = args(0) // First argument
    }

    // Tiempo de inicio
    val currentDateTime: LocalDateTime = LocalDateTime.now()

    // Se lee la molecula y se guardan los datos en un dataframe
    val rMol = new Molecule(inOpF)

    val sim = new Simetria(rMol.cAtF, rMol.molF)

    // Se añade el tiempo de entrada
    val molF1 = sim.molF.withColumn(Par.cTime, lit(currentDateTime))

    //Se imprime los resultados como parquet
    molF1.coalesce(1).write.mode("overwrite").parquet(outputF)
    rMol.cAtF.write.mode("append").parquet(outputF)

    spark.stop()
  }

}
