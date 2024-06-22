package VelESPro

import breeze.linalg.{DenseMatrix, eigSym}
import breeze.linalg.eigSym.EigSym

import scala.math._

class Simetria(mol: Molecule, n_at:Int) {

  def mat_iner(mol: Molecule, n_at: Int): DenseMatrix[Double] = {
    var m_inercia = DenseMatrix.zeros[Double](3,3)
    for (i <- 0 until n_at) {
      m_inercia(0, 0) +=  mol.mass(i) * (pow(mol.coor(1,i)-mol.c_mas(1),2) + pow(mol.coor(2,i)-mol.c_mas(2),2))
      m_inercia(1, 1) +=  mol.mass(i) * (pow(mol.coor(0,i)-mol.c_mas(0),2) + pow(mol.coor(2,i)-mol.c_mas(2),2))
      m_inercia(2, 2) +=  mol.mass(i) * (pow(mol.coor(0,i)-mol.c_mas(0),2) + pow(mol.coor(1,i)-mol.c_mas(1),2))
      m_inercia(0, 1) -=  mol.mass(i)*(mol.coor(0,i) + mol.c_mas(0))*(mol.coor(1,i) + mol.c_mas(1))
      m_inercia(0, 2) -=  mol.mass(i)*(mol.coor(0,i) + mol.c_mas(0))*(mol.coor(2,i) + mol.c_mas(2))
      m_inercia(1, 2) -=  mol.mass(i)*(mol.coor(1,i) + mol.c_mas(1))*(mol.coor(2,i) + mol.c_mas(2))
    }
    m_inercia(1,0)=m_inercia(0,1)
    m_inercia(2,0)=m_inercia(0,2)
    m_inercia(2,1)=m_inercia(1,2)

    return m_inercia
  }

  val dist = new dist()
  def m_distancia(mol: Molecule): DenseMatrix[Double] = {
    var m_dist: DenseMatrix[Double] = DenseMatrix.zeros[Double](n_at, n_at)
    for (i <- 0 until n_at-1) {
      for (j <- i + 1 until n_at) {
        m_dist(i,j)=dist.dst(mol.coord_at(i,::).t,mol.coord_at(j,::).t)
      }
    }
    return m_dist
  }

  val A: DenseMatrix[Double] = mat_iner(mol, n_at) // matriz de inercia
  val EigSym(lambda, evs) = eigSym(A)
}