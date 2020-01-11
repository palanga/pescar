package gob.datos.consumer

import gob.datos.consumer.csv.parser
import gob.datos.consumer.http.client
import gob.datos.consumer.http.client.HttpClient
import gob.datos.consumer.persistence.record.RecordPersistence
import zio.blocking.Blocking
import zio.console.Console
import zio.stream.ZStream
import zio.{ App, ZEnv, ZIO, ZManaged }

object Main extends App {

  override def run(args: List[String]): ZIO[ZEnv, Nothing, Int] =
    loadDataFromCsv
      .flatMap(RecordPersistence.module.saveMany)
      .tap(savedRecords => zio.console.putStrLn("Records saved:\n" ++ savedRecords.mkString("\n")))
      .tap(savedRecords => zio.console.putStrLn("Total: " ++ savedRecords.size.toString))
      .as(ExitStatus.Success)
      .provideSomeManaged(dependencies)
      .orDie

  /**
   * Read the whole file, parse it and collect to a either a list of success or a list of parse failures.
   *
   * The motivation behind this is because if we save the data as it's coming we would have the risk of, at the
   * presence of some error, stop saving the subsequent data and therefore leaving the database in a partial and
   * undesired state.
   *
   * Consider using a Stream based solution if the file is too long.
   */
  private val loadDataFromCsv = {

    def parse(unparsedCsvLines: List[String]) =
      unparsedCsvLines
        .map(csv.parser.parseLine)
        .zipWithIndex
        .partitionMap { case (either, index) => either.left.map(Indexed(index, _)) } match {
        case (Nil, landings) => Right(landings)
        case (failures, _)   => Left(failures)
      }

    // 5489330 bytes
    // 41340 lines (excluding the header)
    io.file
      .open("/Users/palan/Downloads/captura-puerto-flota-2010-2018-utf8.csv")
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
   * Fetch all the data and collect to a list.
   *
   * The motivation behind this is because if we save the data as it's coming we would have the risk of, at the
   * presence of some error, stop saving the subsequent data and therefore leaving the database in a partial and
   * undesired state.
   *
   * Consider using a pure stream solution if the size of the data is too big.
   */
  private val loadDataFromApi = {

    import util.syntax.zio._

    val INITIAL_OFFSET = 0
    val PAGE_SIZE      = 100

    // TODO investigate ZStream.paginate
    ZStream
      .iterate(INITIAL_OFFSET)(_ + PAGE_SIZE)
      .map(makeRequest(Constants.ResourceId.DESEMBARQUE_DE_CAPTURA_DE_ESPECIES_MARÍTIMAS_2019)(PAGE_SIZE))
      .mapMPar_(4, HttpClient.module.fetch)
      .map(_.result.records)
      .takeWhile(_.nonEmpty)
      .runCollect
      .map(_.flatten)

  }

  private def makeRequest(resourceId: types.ResourceId)(pageSize: Int)(offset: Int) =
    client.types.Request(
      Constants.Url.DATOS_AGROINDUSTRIA_GOB_AR,
      types.RequestBody(pageSize, offset, resourceId),
    )

  object ExitStatus {
    val Success = 0
  }

  // TODO maybe we can use some other combinators to compose dependencies
  private val dependencies =
    for {
      env    <- ZManaged.environment[Blocking with Console]
      config <- config.ConfigLoader.test.toManaged_
      doobie <- persistence.record.DoobieRecordPersistence.makeManaged(config.db)
      sttp   <- http.client.SttpClient.makeManaged
    } yield {
      new Blocking with Console with HttpClient with RecordPersistence {
        override val blocking          = env.blocking
        override val console           = env.console
        override val recordPersistence = doobie.recordPersistence
        override val httpClient        = sttp.httpClient
      }
    }

}
