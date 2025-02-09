package VelESPro

import Parameters.Par
import Config_check._
import VelESPro.App.spark
import org.apache.spark.sql.functions._
import com.typesafe.config.{Config, ConfigFactory}
import org.apache.spark.sql.DataFrame
import spark.implicits._

class Molecule(in_op_f : String) {

  // Se inicia la clase de las funciones de molecula
  private val f_mol = new Func_molecule
  private val error = new Error

  // Se lee el fichero de entrada para las opciones de la molecula
  private val t_f = error.check_file(in_op_f)
  private val config: Config = ConfigFactory.parseString(t_f)
  val config_mol: Config = config.getConfig("molecule")
  private val li_molec = config_mol.getAnyRefList("name").toArray().map(_.toString)
  private val n_at_molec = li_molec.map(mol => config_mol.getInt(mol + ".num_atom"))
  private val t_at_nam = (li_molec zip n_at_molec).sortBy(x => x._1)
  private val mol_op_bohr = li_molec.map(mol => config_mol.getOBoolean(mol + ".option.bohr"))
  private val list_bol_bohr = mol_op_bohr.map(x => if (x.nonEmpty) x.get else false)
  private val map_mol_bolbohr: Map[String, Boolean] = li_molec.zip(list_bol_bohr).toMap
  private val df_bohr = map_mol_bolbohr.toSeq.toDF(Par.c_name, "bbohr")

  // Se crea el dataframe de las moleculas
  private val moll = li_molec.map(mol => molec(mol, config_mol.getString(mol + ".method"), config_mol.getString(mol + ".basis set"),
    config_mol.getInt(mol + ".charge"), config_mol.getInt(mol + ".multiplicity")))
  private val mol_ini = moll.toList.toDS()

  // Se lee la basis set
  private val basis = error.readc_conf_file(config_mol.getString("water.basis set"))
  private val basis_set = basis match {
    case Left(e) => spark.emptyDataFrame //En caso de error se saca un dataframe vacio
    case Right(r) => basis.right.get.toDF()
  }

  // Se leen las coordenadas como un dataframe
  private val ruta_c = System.getProperty("user.dir")
  private val lf_coord = li_molec.map(x => ruta_c + "/" + config_mol.getString(x + ".coord")).toSeq
  private val c_ini = f_mol.read_coord(lf_coord, li_molec)

  // Se comprueba que la suma del numero de atomos del fichero de config y el de los ficheros es igual
  error.diff_n_molec(n_at_molec.sum, c_ini.count().toInt)

  // Se pasan a coordenadas atomicas en el caso de que no exista la opcion: borh = true
  private val c_ini_b = c_ini.join(df_bohr, Seq(Par.c_name))
  private val c_at_1 = c_ini_b.withColumn(Par.c_cx, when(col("bbohr") === false, col(Par.c_cx).divide(Par.c_bohr)).otherwise(col(Par.c_cx)))
    .withColumn(Par.c_cy, when(col("bbohr") === false, col(Par.c_cy).divide(Par.c_bohr)).otherwise(col(Par.c_cy)))
    .withColumn(Par.c_cz, when(col("bbohr") === false, col(Par.c_cz).divide(Par.c_bohr)).otherwise(col(Par.c_cz)))
    .drop("bbohr")

  // Se añade la masa de cada atomo y su numero atomico
  private val c_at_2: DataFrame = c_at_1.withColumn(Par.c_m_at, f_mol.masa(col(Par.c_atom)))
    .withColumn(Par.c_nat, f_mol.num_atomic(col(Par.c_atom)))

  //Se añade las basis set
  private val c_at_3 = if (basis_set.count() > 0) {
    c_at_2.join(basis_set, Seq(Par.c_atom),"left_outer")
  } else {
    c_at_2
  }

  // Se añade columna con el id de los atomos
  private val c_at_4 = c_at_3.orderBy(asc("Name")).withColumn("AtomID", monotonically_increasing_id)

  // Se añade la columna con las distancias
  private val inic_coord = c_at_4.select(col("AtomID") as "AtomID",
    col("X") as "X_inic",
    col("Y") as "Y_inic",
    col("Z") as "Z_inic",
    col("Num_at") as "Num_at_inic",
    col("Name") as "Name_inic"
  )
  private val fin_coord = c_at_4.select(col("AtomID") as "AtomID_fin",
    col("X") as "X_fin",
    col("Y") as "Y_fin",
    col("Z") as "Z_fin",
    col("Num_at") as "Num_at_fin",
    col("Name") as "Name_fin"
  )
  private val dist_m = inic_coord.join(fin_coord, col("Name_inic") === col("Name_fin"))
  private val dist_m1 = dist_m.withColumn("At1->At2", concat(col("AtomID"), lit("_"), col("AtomID_fin")))
    .withColumn("Distances", f_mol.eucDistance(col("X_inic"),col("Y_inic"),col("Z_inic"),col("X_fin"),col("Y_fin"),col("Z_fin")))
  private val dist_mm = dist_m1.drop("Name_fin").drop("X_inic").drop("Y_inic").drop("Z_inic").drop("X_fin").drop("Y_fin").drop("Z_fin").withColumnRenamed("Name_inic",Par.c_name)
  // dataframe de la matriz de distancias
  private val df_m_dist = dist_mm.groupBy("AtomID","Name").pivot("AtomID_fin").agg(sum("Distances")).orderBy("AtomID")
  private val name_cols = df_m_dist.drop("AtomID", "Dist_at", "Name").columns
  private val df_m_dist1 = df_m_dist.withColumn("Dist_at",array(name_cols.map(col): _*)).select("AtomID", "Dist_at")
  val c_at_f: DataFrame = c_at_4.join(df_m_dist1,Seq("AtomID"))

  // Se añade la masa y el centro de masas al dataframe de la molecula
  private val df_s_mass = c_at_f.groupBy(Par.c_name).agg(sum(Par.c_m_at).alias("T_Mass"))
  private val df_cm = c_at_f.groupBy(Par.c_name).agg(sum($"X" * $"Mass").alias("MassXX"),sum($"Y" * $"Mass").alias("MassYY"),sum($"Z" * $"Mass").alias("MassZZ"))
  private val df_cm1 = df_s_mass.join(df_cm,Seq(Par.c_name))
  private val df_cm2 = df_cm1.withColumn(Par.c_cm, array($"MassXX" / $"T_Mass", $"MassYY" / $"T_Mass", $"MassZZ" / $"T_Mass"))
  private val df_cm3 = mol_ini.join(df_cm2,Seq(Par.c_name))
  private val mol_1 = df_cm3.select(Par.c_name,"method","basis_set","charge","multiplicity",Par.c_m_m,Par.c_cm).toDF()

  // Se añade la repulsion electrica de cada molecula
  private val E_rep = dist_mm.filter($"AtomID" < $"AtomID_fin").groupBy(Par.c_name).agg(sum($"Num_at_inic" * $"Num_at_fin" / $"Distances").alias("E_rep"))
  val mol_f: DataFrame = mol_1.join(E_rep, Seq(Par.c_name))
}
