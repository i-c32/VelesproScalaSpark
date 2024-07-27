package VelESPro

import Parameters.Par
import VelESPro.App.spark
import org.apache.spark.sql.functions._
import com.typesafe.config.{Config, ConfigFactory}
import org.apache.spark.sql.{DataFrame, Dataset}
import spark.implicits._

case class molec(name: String, method: String, basis_set: String, charge: Int, multiplicity: Int)

class Molecule(in_op_f : String, in_coord_f: String) {

  // Se crea la función para obtener la masa
  private val masa = udf((at: String) => at match {
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
  })

  // Se crea la funcion para obtener el numero atomico de los atomos
  private val num_atomic = udf((at: String) => at match {
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
  })

  // Se lee el fichero de entrada para las opciones de la molecula
  private val ff = scala.io.Source.fromFile(in_op_f)
  private val fil = ff.mkString
  ff.close()
  private val config: Config = ConfigFactory.parseString(fil)
  val config_mol: Config = config.getConfig("molecule")

  // Se crea el dataframe de la molecula
  private val moll = molec(config_mol.getString("name"),config_mol.getString("method"),config_mol.getString("basis set"),
    config_mol.getInt("charge"),config_mol.getInt("multiplicity"))
  private val mol_ini = Seq(moll).toDS()

  // Se lee la basis set
  private val m_line = true
  private val basis_set = spark.read.option("multiLine", m_line).json(Par.ruta_basis +config_mol.getString("basis set"))

  // Se leen las coordenadas como un dataframe
  private val c_ini = spark.read
    .format("csv")
    .option("header", "true")
    .option("delimiter", ";")
    .schema(Par.schema_coord)
    .load(in_coord_f)

  // Se pasan a coordenadas atomicas
  private val c_at_1 = c_ini.withColumn("X", col("X").divide(Par.c_bohr))
    .withColumn("Y", col("Y").divide(Par.c_bohr))
    .withColumn("Z", col("Z").divide(Par.c_bohr))

  // Se añade la masa de cada atomo y su numero atomico
  private val c_at_2: DataFrame = c_at_1.withColumn("Mass", masa(col("Atom")))
    .withColumn("Num_at", num_atomic(col("Atom")))

  //Se añade las basis set
  val c_at_f: DataFrame = c_at_2.join(basis_set, Seq("Atom"))
  val coord_id = c_at_f.select("Atom", "X", "Y", "Z")withColumn("AtomID", monotonically_increasing_id) // Se crea una columna con un id de los atomos

  // Se añade la columna con las distancias
  // Contruct the map for the transpose the coordinates
  val t_df = new transformations_df
  val l_atoms = coord_id.select("Atom").collect().map(_(0).toString).toList
  val seq_atom = (0 until config_mol.getInt("num atom")).toList.map(_.toString)
  val M_inic = Map( "AtomID" -> "Coord")
  val M_atom = (seq_atom zip l_atoms).toMap
  val M_final = M_inic ++ M_atom
  val trans_DF = t_df.TransposeDF(coord_id, Seq("X", "Y", "Z"), "AtomID")
  val newNames = t_df.mapFields(trans_DF, M_final)

  // Se añade la masa y el centro de masas al dataframe de la molecula
  private val sum_masa =  c_at_f.agg(sum("Mass")).first.get(0)
  private val cmas_x = c_at_f.withColumn("Massx", col("X") * col("Mass")).agg(sum("Massx")/sum_masa).first.get(0)
  private val cmas_y = c_at_f.withColumn("Massy", col("Y") * col("Mass")).agg(sum("Massy")/sum_masa).first.get(0)
  private val cmas_z = c_at_f.withColumn("Massz", col("Z") * col("Mass")).agg(sum("Massz")/sum_masa).first.get(0)

  val mol_f: DataFrame = mol_ini.withColumn("Mass", lit(sum_masa))
    .withColumn(
      "Center_mass",
      struct(
        lit(cmas_x).as("X"),
        lit(cmas_y).as("Y"),
        lit(cmas_z).as("Z"),
      ).as("Center_mass")
    )

}
