package VelESPro

import Parameters.Par
import VelESPro.App.spark
import org.apache.spark.sql.functions.col
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

import scala.math.BigDecimal.RoundingMode

case class coord(X: Double, Y: Double, Z: Double)

class MoleculeTest extends AnyWordSpec with Matchers {

  import spark.implicits._

  val in_mol1 = "/home/iveloso/IdeaProjects/Scala_VelESPro/src/test/resources/Input/input1.vel"
  val nombre = "water"
  val r_mol = new Molecule(in_mol1)

  "Molecule with only one molecule" should {
    "obtain the configuration" which {
      "obtain the basis set" in {
        r_mol.config_mol.getString(nombre + ".basis set") shouldBe "STO-3G"
      }
      "obtain the method" in {
        r_mol.config_mol.getString(nombre + ".method") shouldBe "HF"
      }
      "obtain the multiplicity" in {
        r_mol.config_mol.getInt(nombre + ".multiplicity") shouldBe 1
      }
      "obtain the charge" in {
        r_mol.config_mol.getInt(nombre + ".charge") shouldBe 0
      }
    }
  }

  private val mass1 = List(1.00797, 1.00797, 15.9994) //Lista de las masas de los atomos
  private val c_ch1 = List(1, 1, 8) // Lista de la numeros atomicos de los atomos
  //Coordenadas en bohrs en un Dataframe
  private val someDF = Seq(
    coord(1.6380369107226893, 1.1365487595188923, -0.0000000000000000),
    coord(-1.6380369107226893, 1.1365487595188923, -0.0000000000000000),
    coord(0.0000000000000000, -0.14322574511573555, 0.0000000000000000)
  ).toDF()
  //Lista de valores del centro de masas
  private val c_mass1 = List(BigDecimal("0E-15").setScale(15), BigDecimal("-0.000017311920851").setScale(15), BigDecimal("0E-15").setScale(15))
  //Convert the scala bigDecimal to java Bigdecimal that is the return value for spark
  private val c_mass1_j = c_mass1.map(_.bigDecimal)

  it should {
    "obtain the atoms in borhs" in {
      someDF.orderBy("X").collect() shouldBe r_mol.c_at_f.select("X", "Y", "Z").orderBy("X").collect()

    }
    "obtain the atomic number of the atoms" in {
      val c_ch = r_mol.c_at_f.collect().map(_.getAs[Int]("Num_at")).toList
      c_ch shouldBe c_ch1
    }
    "obtain the mass of the atoms" in {
      val masa = r_mol.c_at_f.collect().map(_.getAs[Double]("Mass")).toList
      masa shouldBe mass1
    }
    "obtain the center of mass" in {
      val cast_cm = r_mol.mol_f.withColumn(Par.c_cm, col(Par.c_cm).cast("array<decimal(25,15)>"))
      val c_mass = cast_cm.collect().map(_.getAs[Seq[BigDecimal]]("Center_mass")).apply(0).toList
      c_mass shouldBe c_mass1_j
    }
  }

  val in_mol2 = "/home/iveloso/IdeaProjects/Scala_VelESPro/src/test/resources/Input/input2.vel"
  val nombre2 = "test_at"
  val r_mol1 = new Molecule(in_mol2)

  //Lista de valores del centro de masas
  private val c_mass2 = List(BigDecimal("0.24216318999483818").setScale(15, RoundingMode.HALF_EVEN),
    BigDecimal("-0.080676815136674446").setScale(15, RoundingMode.HALF_EVEN),
    BigDecimal("-0.09521388579237054").setScale(15, RoundingMode.HALF_EVEN))
  private val c_mass2_j = c_mass2.map(_.bigDecimal)

  "Molecule with two molecule" should {
    "obtain the configuration" which {
      "obtain both basis set" in {
        r_mol1.config_mol.getString(nombre + ".basis set") shouldBe "STO-3G"
        r_mol1.config_mol.getString(nombre2 + ".basis set") shouldBe "6-31G"
      }
    }
  }
  it should {
    "obtain the center of mass" in {
      val name = r_mol1.mol_f.collect().map(_.getAs[String]("Name"))
      val cast_cm = r_mol1.mol_f.withColumn(Par.c_cm, col(Par.c_cm).cast("array<decimal(25,15)>"))
      val c_mass = cast_cm.collect().map(_.getAs[Seq[Double]]("Center_mass").toList)
      val map_nm = name.zip(c_mass).toMap
      map_nm("water") shouldBe c_mass1_j
      map_nm("test_at") shouldBe c_mass2_j
    }
  }
}
