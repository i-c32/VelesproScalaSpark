package VelESPro

import Parameters.Par._

import breeze.linalg.{DenseMatrix, DenseVector}

class Read_mol(input_f : String) {

  // Se lee el fichero de entrada
  val fil = scala.io.Source.fromFile(input_f)
  val it = fil.getLines().toList
  fil.close()

  // Se añaden el metodo y la base
  val A1 = it(0).split("\\s+")
  val method = A1(0)
  val basis_set = A1(1)
  // Se guardan las opciones, tienen que estar todas en la misma linea
  val opcion = it(1).split("\\s+")
  // Se lee la carga y la multiplicidad
  val A2 = it(3).split(" ")
  val carga = A2(0).toInt
  val multi = A2(1).toInt
  // Se lee el numero de atomos
  val n_at = it(4).toInt
  // Se crea la matriz de coordenadas
  var coord: DenseMatrix[Double] = DenseMatrix.zeros[Double](n_at, 3)
  // Se leen las coordenadas
  var A = it(5).split("\\s+")
  var at = Array(A(0))
  coord(0, ::) := DenseVector(A(1).toDouble, A(2).toDouble, A(3).toDouble).t

  for (j <- 1 until n_at) {
    A = it(j + 5).split("\\s+")
    at :+= A(0)
    coord(j, ::) := DenseVector(A(1).toDouble, A(2).toDouble, A(3).toDouble).t
  }
}

// Se crea la clase molecula con los datos necesarios que se usaran.
class Molecule(val n_atom: Int, val ch: Int, val multi : Int, val at: Array[String], val coor: DenseMatrix[Double]) {

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

  // funcion para obtener el centro de masas
  def c_mass(coord_at: DenseMatrix[Double], mass: DenseVector[Double]): DenseVector[Double] = {
    var m_tot : Double = 0.0
    for (i <- mass){
      m_tot += i
    }
    var cmass = DenseVector.zeros[Double](3)
    for (i <- 0 until mass.size){
      for (j <- 0 until 3) {
        cmass(j) += mass(i)*coord_at(i,j)
      }
    }
    cmass/m_tot
    return cmass
  }

  // Se cambian a coordenadas atomicas
  val coord_at = coor / c_bohr
  //Se orienta de acuerdo con los ejes de inercia
  val c_at_i = coord_at

  val n_atomic = at.map(x => num_atomic(x)) //numero atomico de los atomos
  val mass = DenseVector(at.map(x => masa(x))) // masa de los atomos
  val a_chg = DenseVector(at.map(x => chg(x))) // carga de los atomos
  val c_mas = c_mass(coord_at, mass) // centro de masa
  val n_at = n_atom // numero de atomos

}
