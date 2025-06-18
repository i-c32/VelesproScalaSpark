package VelESPro

import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.functions._

class OperSimetria {

  def centroInversion(mol: DataFrame, names: Array[String]): DataFrame = {

    val molOpSim = mol.withColumn("X",-col("X")).withColumn("Y",-col("Y")).withColumn("Z",-col("Z"))
      .drop("Mass","GTO","Dist_at","AtomID")

    val molComp = mol.drop("Mass","GTO","Dist_at","AtomID")

    val results = names.map { name =>
      val dfInName = molOpSim.filter(col("Name") === name)
      val dfInName1 = molComp.filter(col("Name") === name)

      val m = dfInName.except(dfInName1)

      val mm1 = 1
    }

    mol

  }

}