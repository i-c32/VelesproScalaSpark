package VelESPro

import Parameters.Par
import ConfigCheck._
import VelESPro.App.spark
import org.apache.spark.sql.functions._
import com.typesafe.config.{Config, ConfigFactory}
import org.apache.spark.sql.DataFrame
import spark.implicits._

import scala.collection.compat.immutable.ArraySeq

class Molecule(inOpFin : String) {

  // Se inicia la clase de las funciones de molecula
  private val fMol = new FuncMolecule
  private val error = new Error

  // Se lee el fichero de entrada para las opciones de la molecula
  private val tF = error.checkFile(inOpFin)
  private val config: Config = ConfigFactory.parseString(tF)
  val configMol: Config = config.getConfig("molecule")
  private val liMolec = configMol.getAnyRefList("name").toArray().map(_.toString)
  private val nAtMolec = liMolec.map(mol => configMol.getInt(mol + ".num_atom"))
  //private val t_at_nam = (liMolec zip nAtMolec).sortBy(x => x._1)
  private val molOpBohr = liMolec.map(mol => configMol.getOBoolean(mol + ".option.bohr"))
  private val listBolBohr = molOpBohr.map(x => x.getOrElse(false))
  private val mapMolBolbohr: Map[String, Boolean] = liMolec.zip(listBolBohr).toMap
  private val dfBohr = mapMolBolbohr.toSeq.toDF(Par.cName, "bbohr")

  // Se crea el dataframe de las moleculas
  private val moll = liMolec.map(mol => Molec(mol, configMol.getString(mol + ".method"), configMol.getString(mol + ".basis set"),
    configMol.getInt(mol + ".charge"), configMol.getInt(mol + ".multiplicity")))
  private val molIni = moll.toList.toDS()

  // Se lee la basis set
  private val basis = error.readcConfFile(configMol.getString("water.basis set"))
  private val basisSet = basis match {
    case Left(_) => spark.emptyDataFrame //En caso de error se saca un dataframe vacio
    case Right(r) => r.toDF()
  }

  // Se leen las coordenadas como un dataframe
  private val rutaC = System.getProperty("user.dir")
  private val lfCoord = liMolec.map(x => rutaC + "/" + configMol.getString(x + ".coord")).toSeq
  private val cIni = fMol.readCoord(lfCoord, liMolec)

  // Se comprueba que la suma del numero de atomos del fichero de config y el de los ficheros es igual
  error.diffNMolec(nAtMolec.sum, cIni.count().toInt)

  // Se pasan a coordenadas atomicas en el caso de que no exista la opcion: borh = true
  private val cIniB = cIni.join(dfBohr, Seq(Par.cName))
  private val cAt1 = cIniB.withColumn(Par.cCx, when(col("bbohr") === false, col(Par.cCx).divide(Par.cBohr)).otherwise(col(Par.cCx)))
    .withColumn(Par.cCy, when(col("bbohr") === false, col(Par.cCy).divide(Par.cBohr)).otherwise(col(Par.cCy)))
    .withColumn(Par.cCz, when(col("bbohr") === false, col(Par.cCz).divide(Par.cBohr)).otherwise(col(Par.cCz)))
    .drop("bbohr")

  // Se añade la masa de cada atomo y su numero atomico
  private val cAt2: DataFrame = cAt1.withColumn(Par.cMAt, fMol.mapMasa(col(Par.cAtom)))
    .withColumn(Par.cNat, fMol.numAtomic(col(Par.cAtom)))

  //Se añade las basis set
  private val cAt3 = if (basisSet.count() > 0) {
    cAt2.join(basisSet, Seq(Par.cAtom),"left_outer")
  } else {
    cAt2
  }

  // Se añade columna con el id de los atomos
  private val cAt4 = cAt3.orderBy(asc("Name")).withColumn("AtomID", monotonically_increasing_id())

  // Se añade la columna con las distancias
  private val inicCoord = cAt4.select(col("AtomID") as "AtomID",
    col("X") as "X_inic",
    col("Y") as "Y_inic",
    col("Z") as "Z_inic",
    col("Num_at") as "Num_at_inic",
    col("Name") as "Name_inic"
  )
  private val finCoord = cAt4.select(col("AtomID") as "AtomID_fin",
    col("X") as "X_fin",
    col("Y") as "Y_fin",
    col("Z") as "Z_fin",
    col("Num_at") as "Num_at_fin",
    col("Name") as "Name_fin"
  )
  private val distM = inicCoord.join(finCoord, col("Name_inic") === col("Name_fin"))
  private val distM1 = distM.withColumn("At1->At2", concat(col("AtomID"), lit("_"), col("AtomID_fin")))
    .withColumn("Distances", fMol.eucDistance(col("X_inic"),col("Y_inic"),col("Z_inic"),col("X_fin"),col("Y_fin"),col("Z_fin")))
  private val distMm = distM1.drop("Name_fin").drop("X_inic").drop("Y_inic").drop("Z_inic").drop("X_fin").drop("Y_fin").drop("Z_fin").withColumnRenamed("Name_inic",Par.cName)
  // dataframe de la matriz de distancias
  private val dfMDist = distMm.groupBy("AtomID","Name").pivot("AtomID_fin").agg(sum("Distances")).orderBy("AtomID")
  private val nameCols = dfMDist.drop("AtomID", "Dist_at", "Name").columns
  private val dfMDist1 = dfMDist.withColumn("Dist_at",array(ArraySeq.unsafeWrapArray(nameCols.map(col)): _*)).select("AtomID", "Dist_at")
  val cAtF: DataFrame = cAt4.join(dfMDist1,Seq("AtomID"))

  // Se añade la masa y el centro de masas al dataframe de la molecula
  private val dfSMass = cAtF.groupBy(Par.cName).agg(sum(Par.cMAt).alias("T_Mass"))
  private val dfCm = cAtF.groupBy(Par.cName).agg(sum($"X" * $"Mass").alias("MassXX"),sum($"Y" * $"Mass").alias("MassYY"),sum($"Z" * $"Mass").alias("MassZZ"))
  private val dfCm1 = dfSMass.join(dfCm,Seq(Par.cName))
  private val dfCm2 = dfCm1.withColumn(Par.cCm, array($"MassXX" / $"T_Mass", $"MassYY" / $"T_Mass", $"MassZZ" / $"T_Mass"))
  private val dfCm3 = molIni.join(dfCm2,Seq(Par.cName))
  private val mol1 = dfCm3.select(Par.cName,"method","basiSet","charge","multiplicity",Par.cMM,Par.cCm).toDF()

  // Se añade la repulsion electrica de cada molecula
  private val E_rep = distMm.filter($"AtomID" < $"AtomID_fin").groupBy(Par.cName).agg(sum($"Num_at_inic" * $"Num_at_fin" / $"Distances").alias("E_rep"))
  val molF: DataFrame = mol1.join(E_rep, Seq(Par.cName))
}
