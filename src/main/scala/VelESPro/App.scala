package VelESPro

import Parameters.Par._
import org.apache.spark.sql.SparkSession

import java.io.{File, FileOutputStream, PrintWriter}
import java.time.LocalDateTime

/**
 * @author ${Ivan}
 */
object App {

  val spark = SparkSession
    .builder()
    .master("local[*]")
    .appName("VelESPro")
    .getOrCreate()

  def IntroPrint():Unit = {

    val writer = new PrintWriter(new File(output_f))

    writer.printf("----------------------------------------------------------------------------------------------------------------\n" +
      "VVVVV           VVVVVVV            llllll EEEEEEEEEEEEEE  SSSSSSSSSSSS PPPPPPPPPPPPPP                           \n" +
      "V:::V           V:::::V            l::::l E::::::::::::E S::::::::::::SP:::::::::::::P                          \n" +
      "V:::V           V:::::V            l::::l E::::::::::::ES::::SSSSS::::SP:::::PPPPP::::P                         \n" +
      "V:::V           V:::::V            l::::l EE::::EEEEE::ES::::S    SSSSSPP::::P    P::::P                        \n" +
      "V:::V           V::::V eeeeeeeeee   l:::l   E:::E   EEEES::::S           P:::P    P::::Prrr   rrrrr     oooooo  \n" +
      "V::::V         V::::Vee::::::::::e  l:::l   E:::E       S::::S           P:::P    P::::Pr::rrr:::::r   o::::::o \n" +
      " V::::V       V::::Ve::::eeeee::::eel:::l   E::::EEEEEE  ::::SSS         P:::PPPPP::::P r:::::::::::r o::::::::o\n" +
      "  V::::V     V::::Ve::::e     e::::el:::l   E:::::::::E  SS:::::SSSS     P:::::::::::P  rr::::rrr::::ro:::oo:::o\n" +
      "   V::::V   V::::V e:::::eeeee:::::el:::l   E:::::::::E    SSS::::::S    P:::PPPPPPPP    r:::r   r:::ro::o  o::o\n" +
      "    V::::V V::::V  e::::::::::::::e l:::l   E::::EEEEEE       SSSSS::S   P:::P           r:::r   rrrrro::o  o::o\n" +
      "     V::::V::::V   e::::eeeeeeeeee  l:::l   E:::E                 S:::S  P:::P           r:::r        o::o  o::o\n" +
      "      V:::::::V    e:::::e          l:::l   E:::E   EEEE          S:::S  P:::P           r:::r        o::o  o::o\n" +
      "       V:::::V     e::::::e        l:::::lEE::::EEEE:::ESSSSSS    S:::SPP:::::PP         r:::r        o:::oo:::o\n" +
      "        V:::V       e::::::eeeeeee l:::::lE::::::::::::ES:::::SSSSS:::SP:::::::P         r:::r        o::::::::o\n" +
      "         V:V         ee::::::::::e l:::::lE::::::::::::ES::::::::::::S P:::::::P         r:::r         o::::::o \n" +
      "          V            eeeeeeeeeee lllllllEEEEEEEEEEEEEE SSSSSSSSSSSS  PPPPPPPPP         rrrrr          oooooo  \n" +
      "----------------------------------------------------------------------------------------------------------------\n")
    val currentDateTime: LocalDateTime = LocalDateTime.now()
    writer.printf("Actual time: " + currentDateTime + "\n")
    writer.printf("\n")
    writer.printf("Electronic structure program developed by Ivan Gonzalez Veloso\n")
    writer.printf("\n")
    writer.printf("\n")
    writer.close()
  }

  def main(args : Array[String]) {

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

    // Se abre el fichero que se va a escribir de manera limpia
    val writer = new PrintWriter(new FileOutputStream(new File(output_f), true))

    // Se escribe el fichero de entrada
    IntroPrint()

    // Se lee la molecula y se guardan los datos en un dataframe
    val r_mol = new Molecule(in_op_f, in_coord_f)

//
//    // Print the input file
//    writer.printf("Input file:\n")
//    writer.printf("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%\n")
//    writer.printf("Method: %s Basis: %s\n", r_mol.method, r_mol.basis_set)
//    for (j <- 0 to r_mol.opcion.size-1) {
//      writer.printf("%s\n", r_mol.opcion(j))
//    }
//    writer.write("%1d %1d\n",mol.ch, mol.multi)
//    for (j <- 0 to r_mol.n_at-1) {
//      writer.print("%s %12.6f %12.6f %12.6f\n", mol.at(j), mol.coor(j)(0), mol.coor(j)(1), mol.coor(j)(2))
//    }
//    writer.print("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%\n")
//
//    // repulsion electronica
//    val e_rep_nuc = f_e_rep_n(r_mol.n_at, mol)
//    writer.print("\n")
//    writer.print("Nuclear repulsion energy =   %12.7f Hartrees\n", e_rep_nuc)
//
//    writer.close()
//
//    //val sim = new Simetria(mol, r_mol.n_at)

    spark.stop()
  }

}
