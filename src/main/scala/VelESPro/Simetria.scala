package VelESPro

import breeze.linalg.{DenseMatrix, DenseVector}

class simetria {

  def inercia(mol: Molecule): Unit = {
    var m_inercia = DenseMatrix.zeros[Double](3,3)
    for (i <- 0 until n_at) {
      m_inercia(0, 0) +=  mol.mass(i) * ((mol.coord(1, i) - mol.c_mass(1)) ** 2 + (mol.coord(2, i) - mol.c_mass(2)) ** 2)
      m_inercia(1, 1) +=  mol.mass(i) * ((mol.coord(0, i) - mol.c_mass(0)) ** 2 + (mol.coord(2, i) - mol.c_mass(2)) ** 2)
      m_inercia(2, 2) +=  mol.mass(i) * ((mol.coord(0, i) - mol.c_mass(0)) ** 2 + (mol.coord(1, i) - mol.c_mass(1)) ** 2)
      m_inercia(0, 1) -=  mol.mass(i)*(mol.coord(0, i) + mol.c_mass(0))*(mol.coord(1, i) + mol.c_mass(1))
      m_inercia(0, 2) -=  mol.mass(i)*(mol.coord(0, i) + mol.c_mass(0))*(mol.coord(2, i) + mol.c_mass(2))
      m_inercia(1, 2) -=  mol.mass(i)*(mol.coord(1, i) + mol.c_mass(1))*(mol.coord(2, i) + mol.c_mass(2))
    }
  }
}

