package testUtils

import org.apache.spark.SparkContext
import org.apache.spark.sql.{SQLContext, SparkSession}
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach, Suite}

trait ContextProvider extends BeforeAndAfterAll with BeforeAndAfterEach {
  self: Suite =>

  @transient var spark2: SparkSession = _

  @transient var sparkContext: SparkContext = _

  @transient var sqlContext: SQLContext = _

  override def beforeAll(): Unit = {
    super.beforeAll()

    spark2 = SparkSession
      .builder()
      .appName("spark session")
      .master("local[*]")
      .getOrCreate()

    sparkContext = spark2.sparkContext

    sqlContext = spark2.sqlContext
  }

  override def afterAll(): Unit = {
    super.afterAll()

    if (spark2 != null) {
      spark2.stop()
    }
  }

}
