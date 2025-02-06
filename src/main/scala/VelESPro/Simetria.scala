package VelESPro

import Parameters.Par
import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.functions._

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

class Simetria(mol: DataFrame) {

  def mat_iner(mol: DataFrame): DataFrame = {

    val m_inercia = mol.withColumn("0_0", col("Mass")*(pow(col("Y"),2)+pow(col("Z"),2)))
      .withColumn("1_1", col("Mass")*(pow(col("X"),2)+pow(col("Z"),2)))
      .withColumn("2_2", col("Mass")*(pow(col("X"),2)+pow(col("Y"),2)))
      .withColumn("0_1", col("Mass")*col("X")*col("Y"))
      .withColumn("0_2", col("Mass")*col("X")*col("Z"))
      .withColumn("1_2", col("Mass")*col("Y")*col("Z"))

    val m_inercia_f = m_inercia.groupBy(Par.c_name).
      agg(sum("0_0").alias("0"), sum("0_1").alias("1"), sum("0_2").alias("2"),
        sum("0_1").alias("3"), sum("1_1").alias("4"), sum("1_2").alias("5"),
        sum("0_2").alias("6"), sum("1_2").alias("7"), sum("2_2").alias("8"))

    m_inercia_f
  }

  val n = mat_iner(mol)

}