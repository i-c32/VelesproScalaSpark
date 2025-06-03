package VelESPro

import Parameters.Par
import org.apache.spark.sql.{Column, DataFrame, DataFrameWriter, Row}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.mockito.scalatest.MockitoSugar
import org.mockito.ArgumentMatchersSugar._

class AppRunnerTest extends AnyWordSpec with Matchers with MockitoSugar {

  "App.run" should {
    "generate output parquet files" in {
      // Mocks
      val dfMock = mock[DataFrame]
      val writerMock = mock[DataFrameWriter[Row]]

      // Mock chaining behavior
      when(dfMock.coalesce(1)).thenReturn(dfMock)
      when(dfMock.withColumn(any[String], any[Column])).thenReturn(dfMock)
      when(dfMock.write).thenReturn(writerMock)
      when(writerMock.mode("overwrite")).thenReturn(writerMock)

      val df2Mock = mock[DataFrame]
      val writer2Mock = mock[DataFrameWriter[Row]]
      when(df2Mock.write).thenReturn(writer2Mock)
      when(writer2Mock.mode("append")).thenReturn(writer2Mock)

      // Molecule stub
      val moleculeMock = mock[Molecule]
      when(moleculeMock.cAtF).thenReturn(df2Mock)
      when(moleculeMock.molF).thenReturn(dfMock)

      // Simetria stub
      val simetriaMock = mock[Simetria]
      when(simetriaMock.molF).thenReturn(dfMock)

      App.run(
        inOpF = "mockInput.json",
        outputF = "mockOutput",
        createMolecule = _ => moleculeMock,
        createSimetria = (_, _) => simetriaMock
      )

      verify(writerMock).parquet("mockOutput")
      verify(writer2Mock).parquet("mockOutput")
    }
  }
}
