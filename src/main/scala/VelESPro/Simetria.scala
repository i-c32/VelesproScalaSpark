package VelESPro

import Parameters.Par
import VelESPro.App.spark
import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.functions._
import spark.implicits._

class Simetria(mol: DataFrame, molec: DataFrame) {

  val oS = new OperSimetria

  // Se centra en el CM
  val molCM = oS.centerMolec(mol, molec)

  // Se borra la columna del centro de masa ya que esta centrado
  val molec1 = molec.drop("Center_mass")

  // Se obtiene el array con las distintas moleculas. (Cambiarlo que se sacaria del json de input)
  val names = mol.select("Name").distinct().as[String].collect()

  val molF = oS.tensorIner(molCM, molec1, names, "Name")

  // SEA
  val distSort = molCM.withColumn("Dist_at_s", array_sort(col("Dist_at")))
  // Se pone el array decimal dependiendo de lo ajustado que queremos el resultado
  val distSort1 = distSort.withColumn("Dist_at_s", col("Dist_at_s").cast(Par.errCompSEA))
  val SEA = distSort1.select("Dist_at_s","AtomID").groupBy("Dist_at_s").agg(collect_set("AtomID").as("C_AtomID")).orderBy("C_AtomID").withColumn("SEA_ID", monotonically_increasing_id())
  val molSEA = distSort1.join(SEA,Seq("Dist_at_s"), "inner")
  val molSEA1 = molSEA.drop("Dist_at_s","C_AtomID")

  val molecSEA = molSEA1.groupBy("Name", "SEA_ID")
    .agg(count("*").as("numAtSEA"))

  val nameIDSEA = molecSEA.filter($"numAtSEA" > 1).select("SEA_ID").distinct().as[String].collect()

  val molecSEA1 = oS.tensorIner(molSEA1, molecSEA, nameIDSEA, "SEA_ID")

  val cInv = oS.centroInversion(molCM, molF, names)

  // Ahora se van a obtener los ejes C2

}