package VelESPro

import VelESPro.App.spark
import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.functions._

class OperSimetria {

  def centroInversion(mol: DataFrame, molec: DataFrame, names: Array[String]): DataFrame = {

    val molOpSim = mol.withColumn("X",-col("X")).withColumn("Y",-col("Y")).withColumn("Z",-col("Z"))
      .drop("Mass","GTO","Dist_at","AtomID")

    val molComp = mol.drop("Mass","GTO","Dist_at","AtomID")

    val results = names.map { name =>
      val dfInName = molOpSim.filter(col("Name") === name)
      val dfInName1 = molComp.filter(col("Name") === name)

      val m = dfInName.except(dfInName1)

      molec.select("Name").filter(col("Name") === name).withColumn("C_inv",lit(m.isEmpty))
    }

    results.reduceOption(_ union _).getOrElse(spark.emptyDataFrame)

  }

}