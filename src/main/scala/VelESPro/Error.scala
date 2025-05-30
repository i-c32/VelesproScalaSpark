package VelESPro

import Parameters.Par
import VelESPro.App.spark
import org.apache.hadoop.fs.{FileSystem, Path}
import org.apache.spark.sql.DataFrame

import java.io.{FileNotFoundException, IOException}
import scala.sys.exit

class Error {

  def diffNMolec(nAtMole: Int, cIni: Int): Unit = {
    if (nAtMole > cIni) {
      Console.err.println("Number of atoms in config file is > number of atoms in coord file")
      exit(1)
    }
    else if (nAtMole < cIni) {
      Console.err.println("Number of atoms in config file is < number of atoms in coord file")
      exit(1)
    }
  }

  def checkFile(filename:String): String = {
    try {
      val ff= scala.io.Source.fromFile(filename)
      val stF = ff.mkString
      ff.close()
      stF
    } catch {
      case _: FileNotFoundException =>
        Console.err.println("ERROR_1001: The config file do not exist, or it is not found in this folder.")
        exit(1)
      case _: IOException =>
        Console.err.println("Had an IOException trying to read that file")
        exit(1)
    }
  }

  def readcConfFile(filename:String): Either[String,DataFrame] = {
    val fs = FileSystem.get(spark.sparkContext.hadoopConfiguration)
    val fileExists = fs.exists(new Path(Par.rutaBasis + filename))
    if (fileExists) {
      val mLine = true
      Right(spark.read.option("multiLine", mLine).json(Par.rutaBasis + filename))
    } else {
        Console.err.println("ERROR_1002: The basis "+Par.rutaBasis+filename+" do not exist, or it is not found in this folder.")
        Left("ERROR_1002")
    }
  }

}
