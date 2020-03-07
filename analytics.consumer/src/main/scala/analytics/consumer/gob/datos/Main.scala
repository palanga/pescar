package analytics.consumer.gob.datos

import analytics.consumer.gob.datos.csv.parser.LineParseError
import analytics.consumer.gob.datos.database.landing.{ module => db }
import analytics.consumer.gob.datos.http.client.types.Request
import analytics.consumer.gob.datos.http.client.{ module => httpClient }
import config.Config
import zio.stream.ZStream
import zio.{ App, ZEnv, ZIO }

object Main extends App {

  /**
   * Consume landings data from datos.gob.ar and store them raw in our database.
   */
  override def run(args: List[String]): ZIO[ZEnv, Nothing, Int] = {

    import utils.zio.syntax.zioops._

    (loadDataFromCsv zipPar loadDataFromApi())
      .map { case (csvData, apiData) => csvData ++ apiData }
      .flatMap(db.saveMany)
      .tapPrint(saved => s"Saved ${saved.size} landings")
      .tapPrintTimed("Total time: ")
      .as(ExitStatus.Success)
      .provideSomeLayer[ZEnv](dependencies)
      .orDie

  }

  val dependencies = db.doobie(Config.test.db) ++ httpClient.sttp

  /**
   * Read the whole file, parse it and collect to a either a list of success or a list of parse failures.
   *
   * The motivation behind this is because if we save the data as it's coming we would have the risk of, at the
   * presence of some error, stop saving the subsequent data and therefore leaving the database in a partial and
   * undesired state.
   *
   * Consider using a Stream based solution if the file is too long.
   */
  val loadDataFromCsv = {

    def parse(unparsedCsvLines: List[String]) =
      unparsedCsvLines
        .map(csv.parser.parseLine)
        .zipWithIndex
        .partitionMap { case (either, index) => either.left.map(Indexed(index + 1, _)) } match {
        case (Nil, landings) => Right(landings)
        case (failures, _)   => Left(failures)
      }

    io.file
      .list(constants.Path.CAPTURA_PUERTO_FLOTA_2010_2018)
      .map(_.drop(1)) // Drop the header
      .map(parse)
      .flatMap(ZIO.fromEither(_).mapError(ParseFailure))

  }

  case class Indexed[A](index: Int, value: A) {
    override def toString: String = s"$index: $value"
  }

  case class ParseFailure(errors: Iterable[Indexed[LineParseError]])
      extends Exception("On lines:\n" ++ errors.mkString("\n"))

  /**
   * Fetch the data and collect to a list.
   *
   * The motivation behind this is because if we save the data as it's coming we would have the risk of, at the
   * presence of some error, stop saving the subsequent data and therefore leaving the database in a partial and
   * undesired state.
   *
   * Consider using a pure stream solution if the size of the data is too big.
   */
  def loadDataFromApi(initialOffset: Int = 0, pageSize: Int = 100) =
    streamDataFromApi(initialOffset, pageSize).runCollect
      .map(_.flatten)

  def streamDataFromApi(initialOffset: Int = 0, pageSize: Int = 100) = {

    import utils.zio.syntax.zioops._

    ZStream
      .iterate(initialOffset)(_ + pageSize)
      .map(makeRequest(constants.ResourceId.DESEMBARQUE_DE_CAPTURA_DE_ESPECIES_MARÍTIMAS_2019)(pageSize))
      .mapMPar_(4, httpClient.fetch)
      .map(_.result.records)
      .takeWhile(_.nonEmpty)

  }

  private def makeRequest(resourceId: types.ResourceId)(pageSize: Int)(offset: Int) =
    Request(
      constants.Url.DATOS_AGROINDUSTRIA_GOB_AR,
      types.RequestBody(pageSize, offset, resourceId),
    )

  object ExitStatus {
    val Success = 0
  }

}
