package VelESPro

import Parameters.Par
import VelESPro.App.spark
import org.apache.spark.sql.functions.{abs, col, expr}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should._

class OperSimetriaTest extends AnyFlatSpec with Matchers{

  import spark.implicits._

  val readmol = spark.read
    .option("header", Par.firstRowHeader)
    .option("delimiter", Par.delimite)
    .csv("src/test/resources/Input/simetry_coord.csv")
  // Se eliminan los espacios vacios y se convierte el array en un double.
  val testmol = readmol.toDF(readmol.columns.map(_.trim): _*).withColumn(
    "Dist_at",
    expr("transform(split(regexp_replace(Dist_at, '\\\\[|\\\\]', ''), ','), x -> cast(trim(x) as double))")
  )

  val testmolec = Seq(
    ("H2O")
  ).toDF("Name")

  "Simetria" should "obtain the inertia matrix" in {
    //val Eps = 1e-5
    val oS = new OperSimetria

    val resultMol = oS.matIner(testmol, "Name").filter(col("Row") === col("Column"))

    val expectedMol= Seq((0, 0, 2.9322820011995119),
      (1, 1, 5.4090993589908090),
      (2, 2, 8.3413813601903222)).toDF("Row","Column","Value_exp")

    val joined = resultMol.join(expectedMol,Seq("Row"))

    assert(joined.filter(abs($"Value" - $"Value_exp") > 1e-6).count() === 0)
  }

}
