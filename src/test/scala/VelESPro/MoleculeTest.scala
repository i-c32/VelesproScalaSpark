package VelESPro

import breeze.linalg.{DenseMatrix, DenseVector}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should._

class MoleculeTest extends AnyFlatSpec with Matchers{

  val input1 = "/home/iveloso/IdeaProjects/Scala_VelESPro/src/test/resources/Input/input1.vel"
  val r_mol = new Read_mol(input1)
  val mol = new Molecule(r_mol.n_at, r_mol.carga, r_mol.multi, r_mol.at, r_mol.coord)

  "Molecule" should "read the molecule and the method" in {
    r_mol.basis_set shouldBe "STO-3G" //check the basis set
    r_mol.method shouldBe "HF" // check the method
    r_mol.multi shouldBe 1 // check the multiplicity
    r_mol.carga shouldBe 0 // check the charge of the molecule
  }

  it should "create a molecule" in {
    //val Eps = 1e-5
    val c_at1= DenseMatrix( Array(1.6380369107226893, 1.1365487595188923, -0.0000000000000000),
      Array(-1.6380369107226893, 1.1365487595188923, -0.0000000000000000),
      Array(0.0000000000000000, -0.14322574511573555, 0.0000000000000000))
    val c_ch1 = DenseVector(Array(1.0000000000000000, 1.0000000000000000, 8.0000000000000000))
    val mass1 = DenseVector(Array(1.00797, 1.00797, 15.9994))
    val c_mass1 = DenseVector(Array(0.00000000, -0.00001732, 0.00000000))

    mol.coord_at shouldBe c_at1 // Check the conversion to borhs
    mol.a_chg shouldBe c_ch1 // Check the charge of the atoms
    mol.mass shouldBe mass1 // Check the mass of the atoms
    mol.c_mas shouldBe c_mass1 // Check the center of mass
    //for (i <- 0 until mol.mass.size) mol.mass(i) should be (mass1(i) +- Eps) // Check the mass of the atoms
  }

}
