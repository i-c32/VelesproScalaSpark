package Parameters

import org.apache.spark.sql.types.{DoubleType, StringType, StructField, StructType}

object Par {
  // Valores para nombres columnas
  val c_name = "Name" // Columna nombre molecula
  val c_atom = "Atom" // Columna nombre atomos
  val c_cx = "X" // Columna coordenadas X
  val c_cy = "Y" // Columna coordenadas Y
  val c_cz = "Z" // Columna coordenadas Z
  val c_m_at = "Mass" // Columna masa de los atomos
  val c_nat = "Num_at" // Columna con el numero atomico
  val c_m_m = "T_Mass" // Columna masa de las moleculas
  val c_time = "Time" // Columna de timestamp
  val c_cm = "Center_mass" // Columna con las coordenas del centro de masas
  val c_bset = "Basis set" // Columna con el nombre de las basis set

  //Schema for the molecule
  val schema_coord: StructType = StructType(Array(
    StructField(c_atom, StringType, nullable = false),
    StructField(c_cx, DoubleType, nullable = false),
    StructField(c_cy, DoubleType, nullable = false),
    StructField(c_cz, DoubleType, nullable = false)
  ))

  // Valores lectura de las coordenadas
  val first_row_header = "true"
  val file_type = "csv"
  val delimite = ";"

  // Valores de errores
  val err_test = 1E-6
  val c_bohr = 0.5291772086
  val h = 6.62606957E-34
  //basis set path
  val ruta_basis: String = "resources/Basis_set/"
  //Archivos de entrada y salida
  val ruta = "resources/Input/"
  val ruta1 = "HF/resources/Output/"
  val in_op_f: String = ruta + "input1.vel"
  val output_f: String = ruta1 + "output1.parquet"
}

