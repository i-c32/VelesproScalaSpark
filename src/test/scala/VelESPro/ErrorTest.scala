package VelESPro

import Parameters.Par
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

import java.io.{File, FileNotFoundException, IOException, PrintWriter}

class ErrorTest extends AnyWordSpec with Matchers {

  val error = new Error
  def createTempJsonFile(name: String, content: String): String = {
    val path = Par.rutaBasis + name
    val pw = new PrintWriter(path)
    pw.write(content)
    pw.close()
    path
  }

  "Errors" should {

    "diffNMolec does not throw when atom counts are equal" in {
      noException should be thrownBy error.diffNMolec(5, 5)
    }

    "diffNMolec throws IllegalArgumentException when config has more atoms" in {
      val thrown = intercept[IllegalArgumentException] {
        error.diffNMolec(6, 5)
      }
      assert(thrown.getMessage == "Number of atoms in config file is > number of atoms in coord file")
    }

    "diffNMolec throws IllegalArgumentException when coord file has more atoms" in {
      val thrown = intercept[IllegalArgumentException] {
        error.diffNMolec(4, 5)
      }
      assert(thrown.getMessage == "Number of atoms in config file is < number of atoms in coord file")
    }

    "checkFile should return contents of an existing file" in {
      val tempFile = File.createTempFile("testfile", ".txt")
      val writer = new PrintWriter(tempFile)
      val content = "This is a test."
      writer.write(content)
      writer.close()

      assert(error.checkFile(tempFile.getAbsolutePath) == content)

      tempFile.deleteOnExit()
    }

    "checkFile should throw FileNotFoundException when file does not exist" in {
      val fakePath = "non_existing_file.txt"
      val thrown = intercept[FileNotFoundException] {
        error.checkFile(fakePath)
      }
      assert(thrown.getMessage.contains("ERROR_1001"))
    }

    "readcConfFile should return Right(DataFrame) for existing file" in {
      val filename = "test_config.json"
      val content = """{"Name": "H2O", "Basis": "STO-3G"}"""
      createTempJsonFile(filename, content)

      val result = error.readcConfFile(filename)
      assert(result.isRight)
      assert(result.toOption.get.columns.contains("Name"))
    }

    "readcConfFile should return Left(error) for non-existing file" in {
      val result = error.readcConfFile("non_existing.json")
      assert(result.isLeft)
      assert(result.swap.toOption.get.startsWith("ERROR_1002"))
    }

  }
}
