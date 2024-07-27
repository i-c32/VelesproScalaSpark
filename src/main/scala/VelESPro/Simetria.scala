package VelESPro

import scala.math._

//class SEA(mol: Molecule) {

  // Se obtiene la matriz de distancia
//  val dist = new dist()

//  def m_dist(mol: Molecule): Array[Array[Double]] = {
//    val m_distancia: Array[Array[Double]] = Array.ofDim[Double](mol.n_at, mol.n_at)
//    for (i <- 0 until mol.n_at-1) {
//      for (j <- i + 1 until mol.n_at) {
//        m_distancia(i)(j)=dist.dst(mol.coord_at(i),mol.coord_at(j))
//        m_distancia(j)(i) = m_distancia(i)(j)
//      }
//    }
//    m_distancia
//  }
//
//  val ma_dist = m_dist(mol)
//  //var m_d_ord: Array[Array[Double]] = Array.ofDim[Double](mol.n_at, mol.n_at)
//  val m_d_ord: Array[Array[Double]] = ma_dist.map(_.sorted)

//}

//class Simetria(mol: Molecule, n_at:Int) {

//  def mat_iner(mol: Molecule, n_at: Int): Array[Array[Double]] = {
//    var m_inercia = Array.ofDim[Double](3,3)
//    for (i <- 0 until n_at) {
//      m_inercia(0)(0) +=  mol.mass(i) * (pow(mol.coord_at(i)(1)-mol.c_mas(1),2) + pow(mol.coord_at(i)(2)-mol.c_mas(2),2))
//      m_inercia(1)(1) +=  mol.mass(i) * (pow(mol.coord_at(i)(0)-mol.c_mas(0),2) + pow(mol.coord_at(i)(2)-mol.c_mas(2),2))
//      m_inercia(2)(2) +=  mol.mass(i) * (pow(mol.coord_at(i)(0)-mol.c_mas(0),2) + pow(mol.coord_at(i)(1)-mol.c_mas(1),2))
//      m_inercia(0)(1) -=  mol.mass(i) * (mol.coord_at(i)(0)-mol.c_mas(0)) * (mol.coord_at(i)(1)-mol.c_mas(1))
//      m_inercia(0)(2) -=  mol.mass(i) * (mol.coord_at(i)(0)-mol.c_mas(0)) * (mol.coord_at(i)(2)-mol.c_mas(2))
//      m_inercia(1)(2) -=  mol.mass(i) * (mol.coord_at(i)(1)-mol.c_mas(1)) * (mol.coord_at(i)(2)-mol.c_mas(2))
//    }
//    m_inercia(1)(0)=m_inercia(0)(1)
//    m_inercia(2)(0)=m_inercia(0)(2)
//    m_inercia(2)(1)=m_inercia(1)(2)
//
//    return m_inercia
//  }
//
//  val in_m: Array[Array[Double]] = mat_iner(mol, n_at) // matriz de inercia
//
//  val sea = new SEA(mol)
//}