package VelESPro

import breeze.linalg.{DenseMatrix, DenseVector}

import scala.math.{pow, sqrt}

class dist {
  // Calculo de la distancia
  def dst(coord1: DenseVector[Double], coord2: DenseVector[Double]): Double = {

    val dst = sqrt(pow(coord2(0)-coord1(0),2) + pow(coord2(1)-coord1(1),2) + pow(coord2(2)-coord1(2),2))
    return dst

  }
}