package Parameters

import org.apache.spark.sql.types.{DoubleType, StringType, StructField, StructType}

object Par {
  //Schema for the molecule
  val schema_coord: StructType = StructType(Array(
    StructField("Atom", StringType, nullable = false),
    StructField("X", DoubleType, nullable = false),
    StructField("Y", DoubleType, nullable = false),
    StructField("Z", DoubleType, nullable = false)
  ))

  // Valores de errores
  val err_test = 1E-6
  val c_bohr = 0.5291772086
  val h = 6.62606957E-34
  //basis set path
  val ruta_basis: String = "resources/Basis_set/"
  //Archivos de entrada y salida
  private val ruta = "/home/iveloso/IdeaProjects/Scala_VelESPro/"
  val in_op_f: String = ruta + "resources/Input/input1.vel"
  val in_coord_f: String = ruta + "resources/Input/input1_coord.vel"
  val output_f: String = ruta + "resources/Output/output1.out"
}

