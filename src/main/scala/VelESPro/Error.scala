package VelESPro

import Parameters.Par
import VelESPro.App.spark
import org.apache.hadoop.fs.{FileSystem, Path}
import org.apache.spark.sql.DataFrame

import java.io.{FileNotFoundException, IOException}
import scala.sys.exit
import scala.util.Using

class Error {

  def diffNMolec(nAtMole: Int, cIni: Int): Unit = {
    if (nAtMole != cIni) {
      val msg = if (nAtMole > cIni)
        "Number of atoms in config file is > number of atoms in coord file"
      else
        "Number of atoms in config file is < number of atoms in coord file"

      throw new IllegalArgumentException(msg)
    }
  }

  def checkFile(filename: String): String = {
    Using(scala.io.Source.fromFile(filename)) { source =>
      source.mkString
    }.recover {
      case _: FileNotFoundException =>
        throw new FileNotFoundException(
          s"ERROR_1001: The config file '$filename' does not exist or was not found in the folder."
        )
    }.get
  }

  def readcConfFile(filename:String): Either[String,DataFrame] = {

    val fullPath = Par.rutaBasis + filename
    val fs = FileSystem.get(spark.sparkContext.hadoopConfiguration)

    if (fs.exists(new Path(fullPath))) {
      val mLine = true
      Right(spark.read.option("multiLine", mLine).json(fullPath))
    } else {
        Left(s"ERROR_1002: The basis file '$fullPath' does not exist or is not found in this folder.")
    }
  }

}
