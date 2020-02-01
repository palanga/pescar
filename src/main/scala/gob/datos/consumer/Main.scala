package gob.datos.consumer

import gob.datos.consumer.csv.parser
import gob.datos.consumer.database.landing.{ DoobieLandingsDatabase, LandingsDatabase }
import gob.datos.consumer.http.client
import gob.datos.consumer.http.client.HttpClient
import zio.blocking.Blocking
import zio.clock.Clock
import zio.console.Console
import zio.stream.ZStream
import zio.{ App, ZEnv, ZIO, ZManaged }

object Main extends App {

  /**
   * Consume landings data from datos.gob.ar and store them raw in our database.
   */
  override def run(args: List[String]): ZIO[ZEnv, Nothing, Int] = {

    import util.syntax.zioops._

    (loadDataFromCsv zipPar loadDataFromApi)
      .map { case (csvData, apiData) => csvData ++ apiData }
      .flatMap(LandingsDatabase.module.saveMany)
      .tapPrint(saved => s"Saved ${saved.size} landings")
      .tapPrintTimed("Total time: ")
      .as(ExitStatus.Success)
      .provideSomeManaged(dependencies)
      .orDie

  }

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

  case class ParseFailure(errors: Iterable[Indexed[parser.LineParseError]])
      extends Exception("On lines:\n" ++ errors.mkString("\n"))

  /**
   * Fetch withInterpreter the data and collect to a list.
   *
   * The motivation behind this is because if we save the data as it's coming we would have the risk of, at the
   * presence of some error, stop saving the subsequent data and therefore leaving the database in a partial and
   * undesired state.
   *
   * Consider using a pure stream solution if the size of the data is too big.
   */
  private val loadDataFromApi = {

    import util.syntax.zioops._

    val INITIAL_OFFSET = 0
    val PAGE_SIZE      = 100

    ZStream
      .iterate(INITIAL_OFFSET)(_ + PAGE_SIZE)
      .map(makeRequest(constants.ResourceId.DESEMBARQUE_DE_CAPTURA_DE_ESPECIES_MARÍTIMAS_2019)(PAGE_SIZE))
      .mapMPar_(4, HttpClient.module.fetch)
      .map(_.result.records)
      .takeWhile(_.nonEmpty)
      .runCollect
      .map(_.flatten)

  }

  private def makeRequest(resourceId: types.ResourceId)(pageSize: Int)(offset: Int) =
    client.types.Request(
      constants.Url.DATOS_AGROINDUSTRIA_GOB_AR,
      types.RequestBody(pageSize, offset, resourceId),
    )

  object ExitStatus {
    val Success = 0
  }

  // TODO will go away in ZIO 1.0.0-RC18
  private val dependencies =
    for {
      env    <- ZManaged.environment[Blocking with Clock with Console]
      config <- config.ConfigLoader.loadYamlConfig.toManaged_
      doobie <- DoobieLandingsDatabase.makeManaged(config.db)
      sttp   <- http.client.SttpClient.makeManaged
    } yield {
      new Blocking with Clock with Console with LandingsDatabase with HttpClient {
        override val blocking         = env.blocking
        override val clock            = env.clock
        override val console          = env.console
        override val landingsDatabase = doobie.landingsDatabase
        override val httpClient       = sttp.httpClient
      }
    }

}
