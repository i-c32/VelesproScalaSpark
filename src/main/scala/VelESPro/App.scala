package VelESPro

import Parameters.Par._
import breeze.linalg.{DenseMatrix, DenseVector}

import java.io.{File, FileOutputStream, PrintWriter}
import scala.math._
import java.time.LocalDateTime

// Se crea la clase molecula con los datos necesarios que se usaran.
class Molecule(val n_atom: Int, val ch: Int, val multi : Int, val at: Array[String], val coord: DenseMatrix[Double]) {

  // Se crea la función para obtener la masa
  def masa(at: String): Double = at match {
    case "H" => 1.00797
    case "He" => 4.0026
    case "Li" => 6.939
    case "Be" => 9.0122
    case "B" => 10.811
    case "C" => 12.01115
    case "N" => 14.0067
    case "O" => 15.9994
    case "F" => 18.9984032
    case "Ne" => 20.183
    case "Na" => 22.98976928
    case "Mg" => 24.312
    case "Al" => 26.9815386
    case "Si" => 28.0855
    case "P" => 30.9737620
    case "S" => 32.064
    case "Cl" => 35.4527
    case "Ar" => 39.948
    case "K" => 39.102
  }

  // Se crea la funcion para obtener el numero atomico de los atomos
  def num_atomic(at: String): Int = at match {
    case "H" => 1
    case "He" => 2
    case "Li" => 3
    case "Be" => 4
    case "B" => 5
    case "C" => 6
    case "N" => 7
    case "O" => 8
    case "F" => 9
    case "Ne" => 10
    case "Na" => 11
    case "Mg" => 12
    case "Al" => 13
    case "Si" => 14
    case "P" => 15
    case "S" => 16
    case "Cl" => 17
    case "Ar" => 18
    case "K" => 19
  }

  // Se crea la funcion para obtener el numero atomico de los atomos
  def chg(at: String): Double = at match {
    case "H" => 1.0
    case "He" => 2.0
    case "Li" => 3.0
    case "Be" => 4.0
    case "B" => 5.0
    case "C" => 6.0
    case "N" => 7.0
    case "O" => 8.0
    case "F" => 9.0
    case "Ne" => 10.0
    case "Na" => 11.0
    case "Mg" => 12.0
    case "Al" => 13.0
    case "Si" => 14.0
    case "P" => 15.0
    case "S" => 16.0
    case "Cl" => 17.0
    case "Ar" => 18.0
    case "K" => 19.0
  }

  def c_mass(coord_at: DenseMatrix[Double], mass: Array[Double]): DenseVector[Double] = {
    var m_tot : Double = 0.0
    for (i <- mass){
      m_tot += i
    }
    var cmass = DenseVector.zeros[Double](3)
    for (i <- 0 to mass.size-1){
      for (j <- 0 to 3) {
        cmass += mass(i)*coord_at(i,j)
      }
    }
    return cmass
  }

  val n_atomic = at.map(x => num_atomic(x)) //numero atomico de los atomos
  val mass = at.map(x => masa(x)) // masa de los atomos
  val a_chg = at.map(x => chg(x)) // carga de los atomos
  val c_mass = c_mass(coord_at, mass) // centro de masa
  val n_at = n_atom // numero de atomos

  // Se cambian a coordenadas atomicas
  val coord_at = coord / c_bohr

}

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

    // Calculo de la distancia
    def dist(coord1 : DenseVector[Double], coord2 : DenseVector[Double]) : Double= {

      val dist = sqrt(pow(coord2(0)-coord1(0),2) + pow(coord2(1)-coord1(1),2) + pow(coord2(1)-coord1(2),2))
      return dist

    }

    // Calculo de la energia de repulsion
    def f_e_rep_n(n_at: Int, mol: Molecule): Double = {
      var ener: Double = 0.0
      for (i <- 0 until n_at-1) {
        for (j <- i+1 until n_at) {
          ener +=  mol.a_chg(i) * mol.a_chg(j) / dist(mol.coord_at(i,::).t,mol.coord_at(j,::).t)
        }
      }
      return ener
    }

    // Se abre el fichero que se va a escribir de manera limpia
    val writer = new PrintWriter(new FileOutputStream(new File(output_f), true))

    // Se escribe el fichero de entrada
    IntroPrint()

    // Se lee el fichero de entrada
    val it = scala.io.Source.fromFile(input_f).getLines().toList

    // Se añaden el metodo y la base
    val A1=it(0).split("\\s+")
    val method = A1(0)
    val basis_set = A1(1)
    // Se guardan las opciones, tienen que estar todas en la misma linea
    val opcion = it(1).split("\\s+")
    // Se lee la carga y la multiplicidad
    val A2=it(3).split(" ")
    val carga=A2(0).toInt
    val multi=A2(1).toInt
    // Se lee el numero de atomos
    val n_at=it(4).toInt
    // Se crea la matriz de coordenadas
    var coord = DenseMatrix.zeros[Double](n_at,3)
    // Se leen las coordenadas
    var A=it(5).split("\\s+")
    var at = Array(A(0))
    coord(0, ::) := DenseVector(A(1).toDouble, A(2).toDouble, A(3).toDouble).t

    for (j <- 1 to n_at-1) {
      A = it(j+5).split("\\s+")
      at :+= A(0)
      coord(j, ::) := DenseVector(A(1).toDouble, A(2).toDouble, A(3).toDouble).t
    }

    // Se crea la molecula
    val mol = new Molecule(carga, multi, at, coord)

    // Print the input file
    writer.printf("Input file:\n")
    writer.printf("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%\n")
    writer.printf("Method: %s Basis: %s\n", method, basis_set)
    for (j <- 0 to opcion.size-1) {
      writer.printf("%s\n", opcion(j))
    }
    writer.printf("%1d %1d\n",mol.ch, mol.multi)
    for (j <- 0 to n_at-1) {
      writer.printf("%s %12.6f %12.6f %12.6f\n", mol.at(j), mol.coord(j,0), mol.coord(j,1), mol.coord(j,2))
    }
    writer.printf("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%\n")

    // repulsion electronica
    val e_rep_nuc = f_e_rep_n(n_at, mol)
    writer.printf("\n")
    writer.printf("Nuclear repulsion energy =   %12.6f Hartrees\n", e_rep_nuc)

    writer.close()
  }

}
