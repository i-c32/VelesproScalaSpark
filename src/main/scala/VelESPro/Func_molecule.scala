package VelESPro

import Parameters.Par
import VelESPro.App.spark
import org.apache.spark.sql.functions.{lit, udf}
import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.expressions.UserDefinedFunction

import scala.math._

case class molec(name: String, method: String, basis_set: String, charge: Int, multiplicity: Int)

class Func_molecule {

  // Se crea la función para obtener la masa
  val masa: UserDefinedFunction = udf((at: String) => at match {
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
    case "Ca" => 40.08
    case "Sc" => 44.956
    case "Ti" => 47.90
    case "V" => 50.942
    case "Cr" => 51.996
    case "Mn" => 54.938
    case "Fe" => 55.847
    case "Co" => 58.933
    case "Ni" => 58.71
    case "Cu" => 63.54
    case "Zn" => 65.37
    case "Ga" => 69.72
    case "Ge" => 72.59
    case "As" => 74.922
    case "Se" => 78.96
    case "Br" => 79.909
    case "Kr" => 83.80
    case "Rb" => 85.47
    case "Sr" => 87.62
    case "Y" => 88.905
    case "Zr" => 91.22
    case "Nb" => 92.906
    case "Mo" => 95.94
    case "Tc" => 98.00
    case "Ru" => 101.07
    case "Rh" => 102.905
    case "Pd" => 106.4
    case "Ag" => 107.870
    case "Cd" => 112.40
    case "In" => 114.82
    case "Sn" => 118.69
    case "Sb" => 121.75
    case "Te" => 127.60
    case "I" => 126.904
    case "Xe" => 131.30
    case "Cs" => 132.90545
    case "Ba" => 137.34
    case "La" => 138.90547
    case "Ce" => 140.116
    case "Pr" => 140.90765
    case "Nd" => 144.242
    case "Pm" => 145.0
    case "Sm" => 150.36
    case "Eu" => 151.964
    case "Gd" => 157.25
    case "Tb" => 158.92535
    case "Dy" => 162.500
    case "Ho" => 164.93032
    case "Er" => 167.259
    case "Tm" => 168.93421
    case "Yb" => 173.054
    case "Lu" => 174.9668
    case "Hf" => 178.49
    case "Ta" => 180.948
    case "W" => 183.85
    case "Re" => 186.2
    case "Os" => 190.2
    case "Ir" => 192.2
    case "Pt" => 195.09
    case "Au" => 196.967
    case "Hg" => 200.59
    case "Tl" => 204.37
    case "Pb" => 207.19
    case "Bi" => 208.980
    case "Po" => 209.0
    case "At" => 210.0
    case "Rn" => 222.0
    case "Fr" => 223.0
    case "Ra" => 226.0
    case "Ac" => 227.0
    case "Th" => 232.038
    case "Pa" => 231.036
    case "U" => 238.029
    case "Np" => 237.0
    case "Pu" => 244.0
    case "Am" => 243.0
    case "Cm" => 247.0
    case "Bk" => 247.0
    case "Cf" => 251.0
    case "Es" => 252.0
    case "Fm" => 257.0
    case "Md" => 258.0
    case "No" => 259.0
    case "Lw" => 262.0
  })

  // Se crea la funcion para obtener el numero atomico de los atomos
  val num_atomic: UserDefinedFunction = udf((at: String) => at match {
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
    case "Ca" => 20
    case "Sc" => 21
    case "Ti" => 22
    case "V" => 23
    case "Cr" => 24
    case "Mn" => 25
    case "Fe" => 26
    case "Co" => 27
    case "Ni" => 28
    case "Cu" => 29
    case "Zn" => 30
    case "Ga" => 31
    case "Ge" => 32
    case "As" => 33
    case "Se" => 34
    case "Br" => 35
    case "Kr" => 36
    case "Rb" => 37
    case "Sr" => 38
    case "Y" => 39
    case "Zr" => 40
    case "Nb" => 41
    case "Mo" => 42
    case "Tc" => 43
    case "Ru" => 44
    case "Rh" => 45
    case "Pd" => 46
    case "Ag" => 47
    case "Cd" => 48
    case "In" => 49
    case "Sn" => 50
    case "Sb" => 51
    case "Te" => 52
    case "I" => 53
    case "Xe" => 54
    case "Cs" => 55
    case "Ba" => 56
    case "La" => 57
    case "Ce" => 58
    case "Pr" => 59
    case "Nd" => 60
    case "Pm" => 61
    case "Sm" => 62
    case "Eu" => 63
    case "Gd" => 64
    case "Tb" => 65
    case "Dy" => 66
    case "Ho" => 67
    case "Er" => 68
    case "Tm" => 69
    case "Yb" => 70
    case "Lu" => 71
    case "Hf" => 72
    case "Ta" => 73
    case "W" => 74
    case "Re" => 75
    case "Os" => 76
    case "Ir" => 77
    case "Pt" => 78
    case "Au" => 79
    case "Hg" => 80
    case "Tl" => 81
    case "Pb" => 82
    case "Bi" => 83
    case "Po" => 84
    case "At" => 85
    case "Rn" => 86
    case "Fr" => 87
    case "Ra" => 88
    case "Ac" => 89
    case "Th" => 90
    case "Pa" => 91
    case "U" => 92
    case "Np" => 93
    case "Pu" => 94
    case "Am" => 95
    case "Cm" => 96
    case "Bk" => 97
    case "Cf" => 98
    case "Es" => 99
    case "Fm" => 100
    case "Md" => 101
    case "No" => 102
    case "Lw" => 103
  })

  //Lectura de las coordenadas
  def read_coord(f_coord: Seq[String], l_nam: Array[String]): DataFrame = {

    val cabez = Seq(Par.c_name, Par.c_atom, Par.c_cx, Par.c_cy, Par.c_cz)
    val mm = f_coord zip l_nam

    val df1 = mm.map(a => spark.read
      .format(Par.file_type)
      .option("header", Par.first_row_header)
      .option("delimiter", Par.delimite)
      .schema(Par.schema_coord)
      .load(a._1).toDF().withColumn(Par.c_name, lit(a._2)))

    df1.reduce((df1, df2) => df1.join(df2, cabez, "full_outer").localCheckpoint(true))

  }

  //Distancia
  val eucDistance: UserDefinedFunction = udf((x1: Double, y1: Double, z1: Double, x2: Double, y2: Double, z2: Double) =>
    sqrt(pow(x2 - x1, 2) + pow(y2 - y1, 2) + pow(z2 - z1, 2))
  )
}
