package VelESPro

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import testUtils.ContextProvider

class MoleculeTest extends AnyWordSpec with Matchers with ContextProvider {

  val in_mol1 = "/home/iveloso/IdeaProjects/Scala_VelESPro/src/test/resources/Input/input1.vel"
  val in_coo1 = "/home/iveloso/IdeaProjects/Scala_VelESPro/src/test/resources/Input/input1_coord.vel"
  val r_mol = new Molecule(in_mol1, in_coo1)

  "Molecule" should {
    "obtain the configuration" which {
      "obtain the basis set" in {
        r_mol.config_mol.getString("basis set") shouldBe "STO-3G"}
      "obtain the method" in {
        r_mol.config_mol.getString("method") shouldBe "HF"}
      "obtain the multiplicity" in {
        r_mol.config_mol.getInt("multiplicity") shouldBe 1}
      "obtain the charge" in {
        r_mol.config_mol.getInt("charge") shouldBe 0}
    }
  }

  val mass1 = List(1.00797, 1.00797, 15.9994) //Lista de las masas de los atomos
  val c_ch1 = List(1, 1, 8) // Lista de la numeros atomicos de los atomos
  //Lista de las coordenadas en bohrs
  val c_x1 = List(1.6380369107226893, -1.6380369107226893, 0.0000000000000000)
  val c_y1 = List(1.1365487595188923, 1.1365487595188923, -0.14322574511573555)
  val c_z1 = List(-0.0000000000000000, 0.0000000000000000, -0.0000000000000000)
  val c_mass1 = Array(0.00000000, -1.7316119591243956E-5, 0.00000000)
  it should {
    "obtain the atoms in borhs" in {
      val c_x = r_mol.c_at_f.collect().map(_.getAs[Double]("X")).toList
      val c_y = r_mol.c_at_f.collect().map(_.getAs[Double]("Y")).toList
      val c_z = r_mol.c_at_f.collect().map(_.getAs[Double]("Z")).toList
      c_x shouldBe c_x1
      c_y shouldBe c_y1
      c_z shouldBe c_z1
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
      val c_mass = r_mol.mol_f.collect().map(_.getAs[Double]("Center mass")).toList
      c_mass shouldBe c_mass1
    }
  }

}
