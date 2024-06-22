package VelESPro

import Parameters.Par._
import breeze.linalg.{DenseMatrix, DenseVector}

import java.io.{File, FileOutputStream, PrintWriter}
import scala.math._
import java.time.LocalDateTime

/**
 * @author ${Ivan}
 */
object App {

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

    val dist = new dist()

    // Calculo de la energia de repulsion
    def f_e_rep_n(n_at: Int, mol: Molecule): Double = {
      var ener: Double = 0.0
      for (i <- 0 until n_at-1) {
        for (j <- i+1 until n_at) {
          ener +=  mol.a_chg(i) * mol.a_chg(j) / dist.dst(mol.coord_at(i,::).t,mol.coord_at(j,::).t)
        }
      }
      return ener
    }

    // Se abre el fichero que se va a escribir de manera limpia
    val writer = new PrintWriter(new FileOutputStream(new File(output_f), true))

    // Se escribe el fichero de entrada
    IntroPrint()

    // Se lee la molecula
    val r_mol = new Read_mol(input_f)

    // Se crea la molecula
    val mol = new Molecule(r_mol.n_at, r_mol.carga, r_mol.multi, r_mol.at, r_mol.coord)

    // Print the input file
    writer.printf("Input file:\n")
    writer.printf("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%\n")
    writer.printf("Method: %s Basis: %s\n", r_mol.method, r_mol.basis_set)
    for (j <- 0 to r_mol.opcion.size-1) {
      writer.printf("%s\n", r_mol.opcion(j))
    }
    writer.printf("%1d %1d\n",mol.ch, mol.multi)
    for (j <- 0 to r_mol.n_at-1) {
      writer.printf("%s %12.6f %12.6f %12.6f\n", mol.at(j), mol.coor(j,0), mol.coor(j,1), mol.coor(j,2))
    }
    writer.printf("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%\n")

    // repulsion electronica
    val e_rep_nuc = f_e_rep_n(r_mol.n_at, mol)
    writer.printf("\n")
    writer.printf("Nuclear repulsion energy =   %12.7f Hartrees\n", e_rep_nuc)

    writer.close()
  }

}
