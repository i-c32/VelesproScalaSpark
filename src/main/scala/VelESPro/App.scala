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
    var in_op_f: String = ""

    // Se lee el nombre del fichero de entrada.
    if (args.length < 1) {
      println("Please provide the input argument.")
      System.exit(1) // Exit if argument is missing
    } else {
      in_op_f = args(0) // First argument
    }

    // Tiempo de inicio
    val currentDateTime: LocalDateTime = LocalDateTime.now()

    // Se lee la molecula y se guardan los datos en un dataframe
    val r_mol = new Molecule(in_op_f)

    val sim = new Simetria(r_mol.c_at_f)

    // Se añade el timpo de entrada
    val mol_f1 = r_mol.mol_f.withColumn(Par.c_time, lit(currentDateTime))

    //Se imprime los resultados como parquet
    mol_f1.coalesce(1).write.mode("overwrite").parquet(output_f)
    r_mol.c_at_f.coalesce(1).write.mode("append").parquet(output_f)

    spark.stop()
  }

}
