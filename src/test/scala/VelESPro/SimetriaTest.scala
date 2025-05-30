package VelESPro

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should._
import Parameters.Par._

class SimetriaTest extends AnyFlatSpec with Matchers{

  val input1 = "src/test/resources/Input/input1.vel"
  val r_mol = new Molecule(input1)
  //val sim = new Simetria(mol, r_mol.n_at)

  "Simetria" should "obtain the inertia matrix" in {
    //val Eps = 1e-5
    val in_mat= Array( Array(2.9322820011995119, 0.0000000000000000, 0.0000000000000000),
      Array(0.0000000000000000, 5.4090993589908090, 0.0000000000000000),
      Array(0.0000000000000000, 0.0000000000000000, 8.3413813601903222))

    //obtain the meain squeare error of the matrix
    val err = new Errores()
    //val M_errores = err.errorM(sim.in_m, in_mat)

    //M_errores should be < err_test// Check the inertia matrix

  }

}
