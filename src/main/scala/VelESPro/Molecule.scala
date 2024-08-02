package VelESPro

import Parameters.Par
import VelESPro.App.spark
import org.apache.spark.sql.functions._
import com.typesafe.config.{Config, ConfigFactory}
import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.expressions.Window
import spark.implicits._

class Molecule(in_op_f : String) {

  // Se inicia la clase de las funciones de molecula
  val f_mol = new Func_molecule

  // Se lee el fichero de entrada para las opciones de la molecula
  private val ff = scala.io.Source.fromFile(in_op_f)
  private val fil = ff.mkString
  ff.close()
  private val config: Config = ConfigFactory.parseString(fil)
  val config_mol: Config = config.getConfig("molecule")
  private val li_molec = config_mol.getAnyRefList("name").toArray().map(_.toString)
  private val n_at_molec = li_molec.map(mol => config_mol.getInt(mol + ".num_atom"))
  private val t_at_nam = li_molec zip n_at_molec

  // Se crea el dataframe de las moleculas
  private val moll = li_molec.map(mol => molec(mol, config_mol.getString(mol + ".method"), config_mol.getString(mol + ".basis set"),
    config_mol.getInt(mol + ".charge"), config_mol.getInt(mol + ".multiplicity")))
  private val mol_ini = moll.toList.toDS()

  // Se lee la basis set
  private val m_line = true
  private val basis_set = spark.read.option("multiLine", m_line).json(Par.ruta_basis +config_mol.getString("water.basis set"))

  // Se leen las coordenadas como un dataframe
  private val lf_coord = li_molec.map(x => Par.ruta + config_mol.getString(x + ".coord")).toSeq
  private val c_ini = f_mol.read_coord(lf_coord, t_at_nam)

  // Se pasan a coordenadas atomicas
  private val c_at_1 = c_ini.withColumn("X", col("X").divide(Par.c_bohr))
    .withColumn("Y", col("Y").divide(Par.c_bohr))
    .withColumn("Z", col("Z").divide(Par.c_bohr))

  // Se añade la masa de cada atomo y su numero atomico
  private val c_at_2: DataFrame = c_at_1.withColumn("Mass", f_mol.masa(col("Atom")))
    .withColumn("Num_at", f_mol.num_atomic(col("Atom")))

  //Se añade las basis set
  val c_at_f: DataFrame = c_at_2.join(basis_set, Seq("Atom"))
  val coord_id = c_at_f.select("Atom", "X", "Y", "Z").withColumn("AtomID", monotonically_increasing_id) // Se crea una columna con un id de los atomos

  // Se añade la columna con las distancias
  // Contruct the map for the transpose the coordinates
  val t_df = new transformations_df
  val l_atoms = coord_id.select("Atom").collect().map(_(0).toString).toList
  val seq_atom = (0 until t_at_nam.map(_._2).sum).toList.map(_.toString)
  val M_inic = Map( "AtomID" -> "Coord")
  val M_atom = (seq_atom zip l_atoms).toMap
  val M_final = M_inic ++ M_atom
  val trans_DF = t_df.TransposeDF(coord_id, Seq("X", "Y", "Z"), "AtomID")
  val newNames = t_df.mapFields(trans_DF, M_final)

  // Se añade la masa y el centro de masas al dataframe de la molecula
  val mm = c_at_f.groupBy("Molec").agg(sum("Mass"))
  val mm1 = c_at_f.groupBy("Molec").agg(sum($"X" * $"Mass").alias("MassX"),sum($"Y" * $"Mass").alias("MassY"),sum($"Z" * $"Mass").alias("MassZ"))
  val mm2 = mm.join(mm1,Seq("Molec"))
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
