package VelESPro

import org.apache.spark.sql.{DataFrame, Dataset}
import org.apache.spark.sql.functions.{col, collect_list, concat_ws}

import scala.math.{pow, sqrt}

class transformations_df {
  //Funcion para hacer la transposicion entre columnas y filas (Hay que tener cuidado que si los nombres son iguales se agrupan)
  def TransposeDF(df: DataFrame, columns: Seq[String], pivotCol: String): DataFrame = {
    val columnsValue = columns.map(x => "'" + x + "', " + x)
    val stackCols = columnsValue.mkString(",")
    val df_1 = df.selectExpr(pivotCol, "stack(" + columns.size + "," + stackCols + ")")
      .select(pivotCol, "col0", "col1")

    val final_df = df_1.groupBy(col("col0")).pivot(pivotCol).agg(concat_ws("", collect_list(col("col1"))))
      .withColumnRenamed("col0", pivotCol)
    final_df
  }

  // Funcion para cambiar los nombres con un map
  def mapFields[T](ds: Dataset[T], fieldNameMap: Map[String, String]): DataFrame = {
    // make sure the fields are present - note this is not a free operation
    val fieldNames = ds.schema.fieldNames.toSet
    val newNames = fieldNameMap.filterKeys(fieldNames).map{
      case (oldFieldName, newFieldName) => col(oldFieldName).as(newFieldName)
    }.toSeq
    ds.select(newNames: _*)
  }
}

class M_op {
  def sum_matrix(A: Array[Array[Double]], B: Array[Array[Double]]): Array[Array[Double]] = {
    val sum_m: Array[Array[Double]] = Array.ofDim[Double](A.length, 3)
    for (i <- 0 until A.length) {
      sum_m(i) = A(i).zip(B(i)).map(x => x._1 + x._2)
    }
    sum_m
  }

  def sus_matrix(A: Array[Array[Double]], B: Array[Array[Double]]): Array[Array[Double]] = {
    val sus_m: Array[Array[Double]] = Array.ofDim[Double](A.length, 3)
    for (i <- 0 until A.length) {
      sus_m(i) = A(i).zip(B(i)).map(x => x._1 - x._2)
    }
    sus_m
  }

}

class errores {
  // Calculo del error de una matriz
  def error_m(A: Array[Array[Double]], B: Array[Array[Double]]): Double = {

    val num_ele = A.length*A(0).length// numero de elementos
    val m_op = new M_op()
    val M_er = m_op.sus_matrix(A,B)
    val M_error = M_er.flatten.map(_.abs).sum/num_ele
    M_error
  }
}