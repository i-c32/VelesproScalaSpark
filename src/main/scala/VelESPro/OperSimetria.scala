package VelESPro

import Parameters.Par
import VelESPro.App.spark
import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.functions._
import spark.implicits._

class OperSimetria {

  val mtOp = new MOp

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

  def centerMolec(mol: DataFrame, molec: DataFrame): DataFrame = {

    // Se centran las moleculas en el centro de masas
    val molF1 = molec.select("Name","Center_mass")
    val dfForName = molF1.select(col("Name"),posexplode(col("Center_mass")).as(Seq("index", "Center_mass")))

    // Pivot on index to turn rows into columns
    val pivoted = dfForName
      .groupBy("Name")
      .pivot("index")
      .agg(first("Center_mass"))

    val CM = pivoted.withColumnsRenamed(Map(
      "0" -> "X1",
      "1" -> "Y1",
      "2" -> "Z1"
    ))

    // Join the two DataFrames on the "Name" column
    val joined = CM.join(mol, Seq("Name"))

    // Subtract X1 from X and create a new column, e.g., "X_diff"
    joined.withColumn("X", col("X") - col("X1")).
      withColumn("Y", col("Y") - col("Y1")).
      withColumn("Z", col("Z") - col("Z1")).
      drop("X1","Y1","Z1")

  }

  def matIner(mol: DataFrame, colID: String): DataFrame = {

    // Se obtiene la matriz de inercia
    val mInercia = mol.withColumn("0_0", col("Mass") * (pow(col("Y"), 2) + pow(col("Z"), 2)))
      .withColumn("1_1", col("Mass") * (pow(col("X"), 2) + pow(col("Z"), 2)))
      .withColumn("2_2", col("Mass") * (pow(col("X"), 2) + pow(col("Y"), 2)))
      .withColumn("0_1", -col("Mass") * col("X") * col("Y"))
      .withColumn("0_2", -col("Mass") * col("X") * col("Z"))
      .withColumn("1_2", -col("Mass") * col("Y") * col("Z"))

    val mInercia1 = mInercia.groupBy(colID).
      agg(sum("0_0").alias("0_0"), sum("0_1").alias("0_1"), sum("0_2").alias("0_2"),
        sum("0_1").alias("1_0"), sum("1_1").alias("1_1"), sum("1_2").alias("1_2"),
        sum("0_2").alias("2_0"), sum("1_2").alias("2_1"), sum("2_2").alias("2_2"))

    // Reshape into multiple rows (pivot-like operation)
    val mInerciaF = mInercia1.selectExpr(
      colID,
      "stack(9, '0', '0',`0_0`, '0', '1',`0_1`, '0', '2', `0_2`, " +
        "'1', '0', `1_0`, '1', '1', `1_1`, '1', '2', `1_2`, " +
        "'2', '0', `2_0`, '2', '1', `2_1`, '2', '2', `2_2`) as (Row, Column, Value)"
    )

    mInerciaF
  }

  def tensorIner(mol: DataFrame, molec: DataFrame, names: Array[String], colID: String): DataFrame = {
    // Matriz de inercia
    val mIner = matIner(mol, colID)

    val results = names.map { name =>
      val dfForName = mIner.filter(col(colID) === name)
      val nMatTot = dfForName.count().toInt
      val nMat = math.sqrt(nMatTot).toInt
      // Generate Identity Matrix as a flattened list
      val identityMatrixValues = Array.tabulate(nMat, nMat) { (i, j) =>
        if (i == j) 1.0
        else 0.0
      }.flatten
      // Convert values into a DataFrame
      val identityDF = identityMatrixValues.zipWithIndex.toSeq.toDF("DiagM", "index")

      val matt1 = dfForName.withColumn("index", monotonically_increasing_id())
      val matt2 = matt1.join(identityDF, Seq("index"))
      val matt3 = mtOp.diag(matt2, nMatTot, colID).filter(col("Row")===col("Column")).withColumn("I_Tensor", lit(Par.convITencm1)/col("Value"))
      matt3.select(col(colID),col("I_Tensor")).groupBy(colID)
        .agg(collect_list("I_Tensor"))
    }

    val InerTensor = results.reduceOption(_ union _).getOrElse(spark.emptyDataFrame)
    molec.join(InerTensor,Seq(colID),"left").withColumnRenamed("collect_list(I_Tensor)","I_Tensor")

  }

}