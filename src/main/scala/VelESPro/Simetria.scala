package VelESPro

import Parameters.Par
import VelESPro.App.spark
import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.functions._
import spark.implicits._

import scala.annotation.tailrec
import scala.collection.mutable.ListBuffer

class Simetria(mol: DataFrame, molec: DataFrame) {

  def matIner(mol: DataFrame): DataFrame = {

    // Se obtiene la matriz de inercia
    val mInercia = mol.withColumn("0_0", col("Mass") * (pow(col("Y"), 2) + pow(col("Z"), 2)))
      .withColumn("1_1", col("Mass") * (pow(col("X"), 2) + pow(col("Z"), 2)))
      .withColumn("2_2", col("Mass") * (pow(col("X"), 2) + pow(col("Y"), 2)))
      .withColumn("0_1", -col("Mass") * col("X") * col("Y"))
      .withColumn("0_2", -col("Mass") * col("X") * col("Z"))
      .withColumn("1_2", -col("Mass") * col("Y") * col("Z"))

    val mInercia1 = mInercia.groupBy(Par.cName).
      agg(sum("0_0").alias("0_0"), sum("0_1").alias("0_1"), sum("0_2").alias("0_2"),
        sum("0_1").alias("1_0"), sum("1_1").alias("1_1"), sum("1_2").alias("1_2"),
        sum("0_2").alias("2_0"), sum("1_2").alias("2_1"), sum("2_2").alias("2_2"))

    // Reshape into multiple rows (pivot-like operation)
    val mInerciaF = mInercia1.selectExpr(
      "Name",
      "stack(9, '0', '0',`0_0`, '0', '1',`0_1`, '0', '2', `0_2`, " +
        "'1', '0', `1_0`, '1', '1', `1_1`, '1', '2', `1_2`, " +
        "'2', '0', `2_0`, '2', '1', `2_1`, '2', '2', `2_2`) as (Row, Column, Value)"
    )

    mInerciaF
  }

  // Matriz de inercia
  val mIner = matIner(mol)
  val mtOp = new MOp
  val names = mIner.select("Name").distinct().as[String].collect()

  val results = names.map { name =>
    val dfForName = mIner.filter(col("Name") === name)
    val nMatTot = dfForName.count().toInt
    val n_mat = math.sqrt(nMatTot).toInt
    // Generate Identity Matrix as a flattened list
    val identityMatrixValues = Array.tabulate(n_mat, n_mat) { (i, j) =>
      if (i == j) 1.0
      else 0.0
    }.flatten
    // Convert values into a DataFrame
    val identityDF = identityMatrixValues.zipWithIndex.toSeq.toDF("DiagM", "index")

    val matt1 = dfForName.withColumn("index", monotonically_increasing_id())
    val matt2 = matt1.join(identityDF, Seq("index"))
    mtOp.diag(matt2, nMatTot).filter(col("Row")===col("Column")).select(col("Name"),col("Value")).groupBy("Name")
      .agg(collect_list("Value").alias("I_Tensor"))
  }

  val InerTensor = results.reduceOption(_ union _).getOrElse(spark.emptyDataFrame)
  val molF = molec.join(InerTensor,Seq("Name"))

  // SEA
  val distSort = mol.withColumn("Dist_at_s", array_sort(col("Dist_at")))
  // Se pone el array decimal dependiendo de lo ajustado que queremos el resultado
  val distSort1 = distSort.withColumn("Dist_at_s", col("Dist_at_s").cast(Par.errCompSEA))
  val SEA = distSort1.select("Dist_at_s","AtomID").groupBy("Dist_at_s").agg(collect_set("AtomID").as("C_AtomID")).orderBy("C_AtomID").withColumn("SEA_ID", monotonically_increasing_id())
  val molSEA = distSort1.join(SEA,Seq("Dist_at_s"), "inner")
  val molSEA1 = molSEA.drop("Dist_at_s","C_AtomID")

  val mm = 1

}