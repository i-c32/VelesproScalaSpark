package VelESPro

import Parameters.Par
import VelESPro.App.spark
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.apache.spark.sql.functions._

class FunctionsTest extends AnyWordSpec with Matchers {

  import spark.implicits._

  "Functions" should {
    "obtain the matrix product" in {
      val mtOp = new MOp

      val df1 = Seq(
        (0, 0, 1.0),
        (0, 1, 2.0),
        (1, 0, 3.0),
        (1, 1, 4.0)
      ).toDF("Row", "Column", "V1")

      val df2 = Seq(
        (0, 0, 5.0),
        (1, 0, 6.0),
        (0, 1, 7.0),
        (1, 1, 8.0)
      ).toDF("Row", "Column", "V2")

      val nMatL = Seq(0, 1)

      val result = mtOp.compMatProd(df1, df2, nMatL).orderBy("index")

      val expected = Seq(
        (1.0 * 5.0 + 2.0 * 6.0), // dot for (0,0)
        (1.0 * 7.0 + 2.0 * 8.0), // dot for (0,1)
        (3.0 * 5.0 + 4.0 * 6.0), // dot for (1,0)
        (3.0 * 7.0 + 4.0 * 8.0)  // dot for (1,1)
      ).zipWithIndex.toDF("Result_exp", "index")

      val joined = result.join(expected, Seq("index"))

      assert(joined.filter(abs($"Result" - $"Result_exp") > 1e-6).count() === 0)
    }
    "obtain the diagonalizatio of the matrix" in {
      val mtOp = new MOp
      val n = 9

      val df = Seq(
        (0, "A", "0", "0", 25.93738153029572),
        (1, "A", "0", "1", -26.10447417472492),
        (2, "A", "0", "2", 0.0),
        (3, "A", "1", "0", -26.10447417472492),
        (4, "A", "1", "1", 59.91837006274636),
        (5, "A", "1", "2", 0.0),
        (6, "A", "2", "0", 0.0),
        (7, "A", "2", "1", 0.0),
        (8, "A", "2", "2", 71.16608473537926)
      ).toDF("index", "Name", "Row", "Column", "Value")

      val result = mtOp.diag(df, n)
      val result1 = result.filter(col("Row")===col("Column")).select("Value","index")

      val expected = Seq(
        (0, 11.78111887),
        (4, 74.07463273),
        (8, 71.16608474)
      ).toDF("index","Value_exp")

      val joined = result1.join(expected,Seq("index"))

      assert(joined.filter(abs($"Value" - $"Value_exp") > 1e-6).count() === 0)
    }
  }
}
