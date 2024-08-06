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

    // Tiempo de inicio
    val currentDateTime: LocalDateTime = LocalDateTime.now()
    //val dist = new dist()

//    // Calculo de la energia de repulsion
//    def e_rep_n(chg1:Double, chg2:Double, coord1:Array[Double], coord2:Array[Double]): Double = chg1*chg2/dist.dst(coord1,coord2)
//    def f_e_rep_n(n_at: Int, mol: Molecule): Double = {
//      var ener: Double = 0.0
//      val mm : Array[Array[Double]] = Array(Array(1,2,3),Array(4,5,6))
//      for (i <- 0 until n_at-1;
//        j <- i+1 until n_at) {
//          ener +=  e_rep_n(mol.a_chg(i), mol.a_chg(j), mol.coord_at(i), mol.coord_at(j))
//        }
//      return ener
//    }

    // Se lee la molecula y se guardan los datos en un dataframe
    val r_mol = new Molecule(in_op_f)

//
//    // repulsion electronica
//    val e_rep_nuc = f_e_rep_n(r_mol.n_at, mol)
//    writer.print("\n")
//    writer.print("Nuclear repulsion energy =   %12.7f Hartrees\n", e_rep_nuc)
//
//    writer.close()
//
//    //val sim = new Simetria(mol, r_mol.n_at)

    // Se añade el timpo de entrada
    val mol_f1 = r_mol.mol_f.withColumn(Par.c_time, lit(currentDateTime))

    //Se imprime los resultados como parquet
    mol_f1.coalesce(1).write.mode("overwrite").parquet(output_f)
    r_mol.c_at_f.coalesce(1).write.mode("append").parquet(output_f)

    spark.stop()
  }

}
