package VelESPro

import Parameters.Par
import VelESPro.App.spark
import org.apache.spark.sql.functions.{lit, udf}
import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.expressions.UserDefinedFunction

import scala.math._

case class Molec (name: String, method: String, basiSet: String, charge: Int, multiplicity: Int)

class FuncMolecule {

  // Se crea la función para obtener la masa
  val mapMasa: UserDefinedFunction = udf((at: String) => {
    val atWeight = Map(
      "H" -> 1.00797,
      "He" -> 4.0026,
      "Li" -> 6.939,
      "Be" -> 9.0122,
      "B" -> 10.811,
      "C" -> 12.01115,
      "N" -> 14.0067,
      "O" -> 15.9994,
      "F" -> 18.9984032,
      "Ne" -> 20.183,
      "Na" -> 22.98976928,
      "Mg" -> 24.312,
      "Al" -> 26.9815386,
      "Si" -> 28.0855,
      "P" -> 30.9737620,
      "S" -> 32.064,
      "Cl" -> 35.4527,
      "Ar" -> 39.948,
      "K" -> 39.102,
      "Ca" -> 40.08,
      "Sc" -> 44.956,
      "Ti" -> 47.90,
      "V" -> 50.942,
      "Cr" -> 51.996,
      "Mn" -> 54.938,
      "Fe" -> 55.847,
      "Co" -> 58.933,
      "Ni" -> 58.71,
      "Cu" -> 63.54,
      "Zn" -> 65.37,
      "Ga" -> 69.72,
      "Ge" -> 72.59,
      "As" -> 74.922,
      "Se" -> 78.96,
      "Br" -> 79.909,
      "Kr" -> 83.80,
      "Rb" -> 85.47,
      "Sr" -> 87.62,
      "Y" -> 88.905,
      "Zr" -> 91.22,
      "Nb" -> 92.906,
      "Mo" -> 95.94,
      "Tc" -> 98.00,
      "Ru" -> 101.07,
      "Rh" -> 102.905,
      "Pd" -> 106.4,
      "Ag" -> 107.870,
      "Cd" -> 112.40,
      "In" -> 114.82,
      "Sn" -> 118.69,
      "Sb" -> 121.75,
      "Te" -> 127.60,
      "I" -> 126.904,
      "Xe" -> 131.30,
      "Cs" -> 132.90545,
      "Ba" -> 137.34,
      "La" -> 138.90547,
      "Ce" -> 140.116,
      "Pr" -> 140.90765,
      "Nd" -> 144.242,
      "Pm" -> 145.0,
      "Sm" -> 150.36,
      "Eu" -> 151.964,
      "Gd" -> 157.25,
      "Tb" -> 158.92535,
      "Dy" -> 162.500,
      "Ho" -> 164.93032,
      "Er" -> 167.259,
      "Tm" -> 168.93421,
      "Yb" -> 173.054,
      "Lu" -> 174.9668,
      "Hf" -> 178.49,
      "Ta" -> 180.948,
      "W" -> 183.85,
      "Re" -> 186.2,
      "Os" -> 190.2,
      "Ir" -> 192.2,
      "Pt" -> 195.09,
      "Au" -> 196.967,
      "Hg" -> 200.59,
      "Tl" -> 204.37,
      "Pb" -> 207.19,
      "Bi" -> 208.980,
      "Po" -> 209.0,
      "At" -> 210.0,
      "Rn" -> 222.0,
      "Fr" -> 223.0,
      "Ra" -> 226.0,
      "Ac" -> 227.0,
      "Th" -> 232.038,
      "Pa" -> 231.036,
      "U" -> 238.029,
      "Np" -> 237.0,
      "Pu" -> 244.0,
      "Am" -> 243.0,
      "Cm" -> 247.0,
      "Bk" -> 247.0,
      "Cf" -> 251.0,
      "Es" -> 252.0,
      "Fm" -> 257.0,
      "Md" -> 258.0,
      "No" -> 259.0,
      "Lw" -> 262.0
    )
    atWeight.getOrElse(at, 0.0)
  })

  // Se crea la funcion para obtener el numero atomico de los atomos
  val numAtomic: UserDefinedFunction = udf((at: String) => {
    val nAtomic = Map(
      "H" -> 1,
      "He" -> 2,
      "Li" -> 3,
      "Be" -> 4,
      "B" -> 5,
      "C" -> 6,
      "N" -> 7,
      "O" -> 8,
      "F" -> 9,
      "Ne" -> 10,
      "Na" -> 11,
      "Mg" -> 12,
      "Al" -> 13,
      "Si" -> 14,
      "P" -> 15,
      "S" -> 16,
      "Cl" -> 17,
      "Ar" -> 18,
      "K" -> 19,
      "Ca" -> 20,
      "Sc" -> 21,
      "Ti" -> 22,
      "V" -> 23,
      "Cr" -> 24,
      "Mn" -> 25,
      "Fe" -> 26,
      "Co" -> 27,
      "Ni" -> 28,
      "Cu" -> 29,
      "Zn" -> 30,
      "Ga" -> 31,
      "Ge" -> 32,
      "As" -> 33,
      "Se" -> 34,
      "Br" -> 35,
      "Kr" -> 36,
      "Rb" -> 37,
      "Sr" -> 38,
      "Y" -> 39,
      "Zr" -> 40,
      "Nb" -> 41,
      "Mo" -> 42,
      "Tc" -> 43,
      "Ru" -> 44,
      "Rh" -> 45,
      "Pd" -> 46,
      "Ag" -> 47,
      "Cd" -> 48,
      "In" -> 49,
      "Sn" -> 50,
      "Sb" -> 51,
      "Te" -> 52,
      "I" -> 53,
      "Xe" -> 54,
      "Cs" -> 55,
      "Ba" -> 56,
      "La" -> 57,
      "Ce" -> 58,
      "Pr" -> 59,
      "Nd" -> 60,
      "Pm" -> 61,
      "Sm" -> 62,
      "Eu" -> 63,
      "Gd" -> 64,
      "Tb" -> 65,
      "Dy" -> 66,
      "Ho" -> 67,
      "Er" -> 68,
      "Tm" -> 69,
      "Yb" -> 70,
      "Lu" -> 71,
      "Hf" -> 72,
      "Ta" -> 73,
      "W" -> 74,
      "Re" -> 75,
      "Os" -> 76,
      "Ir" -> 77,
      "Pt" -> 78,
      "Au" -> 79,
      "Hg" -> 80,
      "Tl" -> 81,
      "Pb" -> 82,
      "Bi" -> 83,
      "Po" -> 84,
      "At" -> 85,
      "Rn" -> 86,
      "Fr" -> 87,
      "Ra" -> 88,
      "Ac" -> 89,
      "Th" -> 90,
      "Pa" -> 91,
      "U" -> 92,
      "Np" -> 93,
      "Pu" -> 94,
      "Am" -> 95,
      "Cm" -> 96,
      "Bk" -> 97,
      "Cf" -> 98,
      "Es" -> 99,
      "Fm" -> 100,
      "Md" -> 101,
      "No" -> 102,
      "Lw" -> 103
    )
    nAtomic.getOrElse(at, 0)
  })

  //Lectura de las coordenadas
  def readCoord(fCoord: Seq[String], lNam: Array[String]): DataFrame = {

    val cabez = Seq(Par.cName, Par.cAtom, Par.cCx, Par.cCy, Par.cCz)
    val mm = fCoord zip lNam

    val df1 = mm.map(a => spark.read
      .format(Par.fileType)
      .option("header", Par.firstRowHeader)
      .option("delimiter", Par.delimite)
      .schema(Par.schemaCoord)
      .load(a._1).toDF().withColumn(Par.cName, lit(a._2)))

    df1.reduce((df1, df2) => df1.join(df2, cabez, "full_outer").localCheckpoint(true))

  }

  //Distancia
  val eucDistance: UserDefinedFunction = udf((x1: Double, y1: Double, z1: Double, x2: Double, y2: Double, z2: Double) =>
    sqrt(pow(x2 - x1, 2) + pow(y2 - y1, 2) + pow(z2 - z1, 2))
  )
}
