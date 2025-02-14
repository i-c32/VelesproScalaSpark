package VelESPro

import Parameters.Par
import VelESPro.App.spark
import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.functions._
import org.apache.spark.mllib.linalg.DenseMatrix
import spark.implicits._

class Simetria(mol: DataFrame) {

  def mat_iner(mol: DataFrame): DataFrame = {

    val m_inercia = mol.withColumn("0_0", col("Mass")*(pow(col("Y"),2)+pow(col("Z"),2)))
      .withColumn("1_1", col("Mass")*(pow(col("X"),2)+pow(col("Z"),2)))
      .withColumn("2_2", col("Mass")*(pow(col("X"),2)+pow(col("Y"),2)))
      .withColumn("0_1", col("Mass")*col("X")*col("Y"))
      .withColumn("0_2", col("Mass")*col("X")*col("Z"))
      .withColumn("1_2", col("Mass")*col("Y")*col("Z"))

    val m_inercia_1 = m_inercia.groupBy(Par.c_name).
      agg(sum("0_0").alias("0_0"), sum("0_1").alias("0_1"), sum("0_2").alias("0_2"),
        sum("0_1").alias("1_0"), sum("1_1").alias("1_1"), sum("1_2").alias("1_2"),
        sum("0_2").alias("2_0"), sum("1_2").alias("2_1"), sum("2_2").alias("2_2"))

    // Reshape into multiple rows (pivot-like operation)
    val m_inercia_2 = m_inercia_1.selectExpr(
      "Name",
      "stack(9, '0', '0',`0_0`, '0', '1',`0_1`, '0', '2', `0_2`, " +
        "'1', '0', `1_0`, '1', '1', `1_1`, '1', '2', `1_2`, " +
        "'2', '0', `2_0`, '2', '1', `2_1`, '2', '2', `2_2`) as (Row, Column, Value)"
    )

    // elementos fuera de la matriz
    val offDim = m_inercia_2.filter(col("Row") =!= col("Column"))
    val offDim0 = offDim.filter(col("Value") =!= 0)
    // Find the maximum value in the "Value" column
    val max_row = offDim0.agg(max("Value").as("Value"),min("Row").as("Row"),first("Column").as("Column"),first("Name").as("Name"))

    // Get the Row and Column values from a specific row (e.g., the first row)
    val rowValue = max_row.select("Row").first()(0)
    val colValue = max_row.select("Column").first()(0)
    val molName = max_row.select("Name").first()(0)

    // Columnas para el valor del angulo
    val val_ang = m_inercia_2.filter(((col("Row") === rowValue && col("Column") === rowValue)
      || (col("Row") === colValue && col("Column") === colValue)
      || (col("Row") === rowValue && col("Column") === colValue)) && (col("Name") === molName))

    val val_ang1 = val_ang.orderBy(col("Row").asc, col("Column").asc).withColumn(
      "nam",
      when((monotonically_increasing_id() % 3) === 0, lit("Aii"))
        .when((monotonically_increasing_id() % 3) === 1, lit("Aij"))
        .otherwise(lit("Ajj"))
    )

    val val_ang2 = val_ang1.groupBy(col("Name")).pivot(col("nam")).agg(first("Value"))
    // Se tiene que hacer 1/2*atan(2*Aij/(Aii-Ajj))
    val val_ang3 = val_ang2.withColumn("theta", atan((col("Aij") * 2) / (col("Aii") * col("Ajj")))/2)
      .withColumn("cos(t)", cos(col("theta")))
      .withColumn("sin(t)", sin(col("theta")))

    // Get the Row and Column values from a specific row (e.g., the first row)
    val ct = val_ang3.select("cos(t)").first()(0).asInstanceOf[Double]
    val st = val_ang3.select("sin(t)").first()(0).asInstanceOf[Double]

    //Define matrix size
    val n = 3 // Change to desired size
    //Se tiene que transformar en integer
    val rowVI = rowValue.toString.toInt
    val colVI = colValue.toString.toInt
    // Generate Identity Matrix as a flattened list
    val identityMatrixValues = Array.tabulate(n, n) { (i, j) =>
      if (i == rowVI && j == rowVI) ct
      else if (i == colVI && j == colVI) ct
      else if (i == rowVI && j == colVI) st
      else if (i == colVI && j == rowVI) -st
      else if (i == j) 1.1
      else 0.0
    }.flatten
    // Convert values into a DataFrame
    val identityDF = identityMatrixValues.zipWithIndex.toSeq.toDF("DiagM", "index")

    val m_inercia_3 = m_inercia_2.filter(col("Name") === "C2").withColumn("index",monotonically_increasing_id())
    val m_inercia_4 = m_inercia_3.join(identityDF, Seq("index"))

    // Matriz de inercia
    val dm_iner = new DenseMatrix(3, 3, m_inercia_1.drop("Name").collect()(1).toSeq.map(_.asInstanceOf[Double]).toArray)

    // Obtener el valor maximo fuera de la diagonal
    // Create a collection of all off-diagonal elements along with their indices
    val offDiagonalWithIndices = (0 until dm_iner.numRows).flatMap { i =>
      (0 until dm_iner.numCols).collect {
        case j if i != j => (i, j, dm_iner(i, j)) // (row index, column index, value)
      }
    }

    // Find the maximum value off the diagonal and its indices
    val (maxRow, maxCol, maxOffDiagonal) = offDiagonalWithIndices.maxBy(_._3)

    //Creacion de la Matriz identidad
    val size = 3
    val indentM = Array.tabulate(size*size)(i => if (i % size == 0) 1.0 else 0.0)

    val dm_i = new DenseMatrix(3, 3, indentM)

    m_inercia_2
  }

  // Matriz de inercia
  val m_iner = mat_iner(mol)

  // SEA
  val dist_sort = mol.withColumn("Dist_at_s", array_sort(col("Dist_at")))
  // Se pone el array decimal dependiendo de lo ajustado que queremos el resultado
  val dist_sort1 = dist_sort.withColumn("Dist_at_s", col("Dist_at_s").cast("array<decimal(25,7)>"))
  val SEA = dist_sort1.select("Dist_at_s","AtomID").groupBy("Dist_at_s").agg(collect_set("AtomID").as("C_AtomID")).orderBy("C_AtomID").withColumn("SEA_ID", monotonically_increasing_id)
  val mol_SEA = dist_sort1.join(SEA,Seq("Dist_at_s"), "inner")
  val mol_SEA1 = mol_SEA.drop("Dist_at_s","C_AtomID")

  val mm = 1

}