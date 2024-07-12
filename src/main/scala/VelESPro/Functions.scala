package VelESPro

import scala.math.{pow, sqrt}

class dist {
  // Calculo de la distancia
  def dst(coord1: Array[Double], coord2: Array[Double]): Double = {

    val dst = sqrt(pow(coord2(0)-coord1(0),2) + pow(coord2(1)-coord1(1),2) + pow(coord2(2)-coord1(2),2))
    dst
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