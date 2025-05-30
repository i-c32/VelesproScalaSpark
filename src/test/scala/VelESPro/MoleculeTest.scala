package VelESPro

import Parameters.Par
import VelESPro.App.spark
import org.apache.spark.sql.functions.col
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import scala.collection.mutable

import scala.math.BigDecimal.RoundingMode

case class coord(X: Double, Y: Double, Z: Double)

class MoleculeTest extends AnyWordSpec with Matchers {

  import spark.implicits._

  val inMol1 = "src/test/resources/Input/input1.vel"
  val nombre = "water"
  val rMol = new Molecule(inMol1)

  "Molecule with only one molecule" should {
    "obtain the configuration" which {
      "obtain the basis set" in {
        rMol.configMol.getString(nombre + ".basis set") shouldBe "STO-3G"
      }
      "obtain the method" in {
        rMol.configMol.getString(nombre + ".method") shouldBe "HF"
      }
      "obtain the multiplicity" in {
        rMol.configMol.getInt(nombre + ".multiplicity") shouldBe 1
      }
      "obtain the charge" in {
        rMol.configMol.getInt(nombre + ".charge") shouldBe 0
      }
    }
  }

  private val mass1 = List(1.00797, 1.00797, 15.9994) //Lista de las masas de los atomos
  private val cCh1 = List(1, 1, 8) // Lista de la numeros atomicos de los atomos
  //Coordenadas en bohrs en un Dataframe
  private val someDF = Seq(
    coord(1.6380369107226893, 1.1365487595188923, -0.0000000000000000),
    coord(-1.6380369107226893, 1.1365487595188923, -0.0000000000000000),
    coord(0.0000000000000000, -0.14322574511573555, 0.0000000000000000)
  ).toDF()
  //Lista de valores del centro de masas
  private val cMass1 = List(BigDecimal("0E-13").setScale(13), BigDecimal("-0.0000173119209").setScale(13), BigDecimal("0E-13").setScale(13))
  //Convert the scala bigDecimal to java Bigdecimal that is the return value for spark
  private val cMass1J = cMass1.map(_.bigDecimal)

  it should {
    "obtain the atoms in borhs" in {
      someDF.orderBy("X").collect() shouldBe rMol.cAtF.select("X", "Y", "Z").orderBy("X").collect()

    }
    "obtain the atomic number of the atoms" in {
      val cCh = rMol.cAtF.orderBy("AtomID").collect().map(_.getAs[Int]("Num_at")).toList
      cCh shouldBe cCh1
    }
    "obtain the mass of the atoms" in {
      val masa = rMol.cAtF.orderBy("AtomID").collect().map(_.getAs[Double]("Mass")).toList
      masa shouldBe mass1
    }
    "obtain the center of mass" in {
      val cast_cm = rMol.molF.withColumn(Par.cCm, col(Par.cCm).cast("array<decimal(25,13)>"))
      val cMass = cast_cm.collect().map(_.getAs[mutable.Seq[BigDecimal]]("Center_mass")).apply(0).toList
      cMass shouldBe cMass1J
    }
  }

  val inMol2 = "src/test/resources/Input/input2.vel"
  val nombre2 = "test_at"
  val rMol1 = new Molecule(inMol2)

  //Lista de valores del centro de masas
  private val cMass2 = List(BigDecimal("0.24216318999483818").setScale(13, RoundingMode.HALF_EVEN),
    BigDecimal("-0.080676815136674446").setScale(13, RoundingMode.HALF_EVEN),
    BigDecimal("-0.09521388579237054").setScale(13, RoundingMode.HALF_EVEN))
  private val cMass2J = cMass2.map(_.bigDecimal)

  "Molecule with two molecule" should {
    "obtain the configuration" which {
      "obtain both basis set" in {
        rMol1.configMol.getString(nombre + ".basis set") shouldBe "STO-3G"
        rMol1.configMol.getString(nombre2 + ".basis set") shouldBe "6-31G"
      }
    }
  }
  it should {
    "obtain the center of mass" in {
      val name = rMol1.molF.collect().map(_.getAs[String]("Name"))
      val castCm = rMol1.molF.withColumn(Par.cCm, col(Par.cCm).cast("array<decimal(25,13)>"))
      val cMass = castCm.collect().map(_.getAs[mutable.Seq[Double]]("Center_mass").toList)
      val mapNm = name.zip(cMass).toMap
      mapNm("water") shouldBe cMass1J
      mapNm("test_at") shouldBe cMass2J
    }
  }
}
