package VelESPro

import Parameters.Par
import VelESPro.App.spark
import org.apache.hadoop.fs.{FileSystem, Path}
import org.apache.spark.sql.DataFrame

import java.io.{FileNotFoundException, IOException}
import scala.sys.exit

class Error {

  def diff_n_molec(n_at_mole: Int, c_ini: Int): Unit = {
    if (n_at_mole > c_ini) {
      Console.err.println("Number of atoms in config file is > number of atoms in coord file")
      exit(1)
    }
    else if (n_at_mole < c_ini) {
      Console.err.println("Number of atoms in config file is < number of atoms in coord file")
      exit(1)
    }
  }

  def check_file(filename:String): String = {
    try {
      val ff= scala.io.Source.fromFile(filename)
      val st_f = ff.mkString
      ff.close()
      st_f
    } catch {
      case e: FileNotFoundException =>
        Console.err.println("ERROR_1001: The config file do not exist, or it is not found in this folder.")
        exit(1)
      case e: IOException =>
        Console.err.println("Had an IOException trying to read that file")
        exit(1)
    }
  }

  def readc_conf_file(filename:String): Either[String,DataFrame] = {
    val fs = FileSystem.get(spark.sparkContext.hadoopConfiguration)
    val fileExists = fs.exists(new Path(Par.ruta_basis + filename))
    if (fileExists) {
      val m_line = true
      Right(spark.read.option("multiLine", m_line).json(Par.ruta_basis + filename))
    } else {
        Console.err.println("ERROR_1002: The basis "+Par.ruta_basis+filename+" do not exist, or it is not found in this folder.")
        Left("ERROR_1002")
    }
  }

}
