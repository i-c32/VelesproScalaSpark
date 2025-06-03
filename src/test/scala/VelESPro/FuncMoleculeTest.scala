package VelESPro

import VelESPro.App.spark
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class FuncMoleculeTest extends AnyWordSpec with Matchers {

  "Functions" should {

    "correctly store values" in {
      val mol = Molec("H2O", "HF", "6-31G", 0, 1)

      assert(mol.name == "H2O")
      assert(mol.method == "HF")
      assert(mol.basiSet == "6-31G")
      assert(mol.charge == 0)
      assert(mol.multiplicity == 1)
    }

    "work for identical values" in {
      val mol1 = Molec("H2O", "HF", "6-31G", 0, 1)
      val mol2 = Molec("H2O", "HF", "6-31G", 0, 1)

      assert(mol1 == mol2)
    }

    "Copy method" in {
      val m = Molec("H2O", "HF", "6-31G", 0, 1)
      val m2 = m.copy(name = "CO2")
      assert(m2.name == "CO2")
    }

    "Molec pattern matching" in {
      val m = Molec("NH3", "DFT", "STO-3G", 0, 1)
      val msg = m match {
        case Molec("NH3", _, _, _, _) => "Found"
        case _ => "Not found"
      }
      assert(msg == "Found")
    }
  }
}
