package Parameters

import org.apache.spark.sql.types.{DoubleType, StringType, StructField, StructType}

object Par {
  // Valores para nombres columnas
  val cName = "Name" // Columna nombre molecula
  val cAtom = "Atom" // Columna nombre atomos
  val cCx = "X" // Columna coordenadas X
  val cCy = "Y" // Columna coordenadas Y
  val cCz = "Z" // Columna coordenadas Z
  val cMAt = "Mass" // Columna masa de los atomos
  val cNat = "Num_at" // Columna con el numero atomico
  val cMM = "T_Mass" // Columna masa de las moleculas
  val cTime = "Time" // Columna de timestamp
  val cCm = "Center_mass" // Columna con las coordenas del centro de masas
  val cBset = "Basis set" // Columna con el nombre de las basis set

  //Schema for the molecule
  val schemaCoord: StructType = StructType(Array(
    StructField(cAtom, StringType, nullable = false),
    StructField(cCx, DoubleType, nullable = false),
    StructField(cCy, DoubleType, nullable = false),
    StructField(cCz, DoubleType, nullable = false)
  ))

  // Valores lectura de las coordenadas
  val firstRowHeader = "true"
  val fileType = "csv"
  val delimite = ";"

  // Valores de errores
  val errTest = 1E-6
  val errDiag = 1E-10
  val errCompSEA = "array<decimal(25,7)>"
  val cBohr = 0.5291772086
  val convITenGHz = 1804.739829300774
  val convITencm1 = 60.19964082287802
  //basis set path
  val rutaBasis: String = "resources/Basis_set/"
}

