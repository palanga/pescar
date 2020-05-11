package analytics.consumer.gob.datos

import analytics.api.types.Location.{ Harbour, Miscellaneous }
import analytics.api.types.Metric.{ Landing => ApiLanding }
import analytics.api.types.{ Fleet, Location, Specie }
import analytics.consumer.gob.datos.constants.Path.{ CAPTURA_PUERTO_FLOTA_2010_2018, CAPTURA_PUERTO_FLOTA_2019 }
import analytics.consumer.gob.datos.csv.parser
import analytics.consumer.gob.datos.csv.parser.LineParseError
import analytics.consumer.gob.datos.database.landing.{ module => db }
import analytics.consumer.gob.datos.http.client.types.Request
import analytics.consumer.gob.datos.http.client.{ module => httpClient }
import analytics.consumer.gob.datos.types.Landing
import config.Config
import reader.csv.{ parse, Indexed }
import utils.GeoLocation
import zio.stream.ZStream
import zio.{ App, ZEnv, ZIO }

object Main extends App {

  /**
   * Consume landings data from datos.gob.ar and store them raw in our database.
   */
  override def run(args: List[String]): ZIO[ZEnv, Nothing, Int] = {

    import io.circe.generic.auto._
    import io.circe.syntax._
    import utils.zio.syntax.zioops._

    (loadDataFromCsv(CAPTURA_PUERTO_FLOTA_2010_2018) zipPar loadDataFromCsv(CAPTURA_PUERTO_FLOTA_2019)).map {
      case (csvData, apiData) => csvData ++ apiData
    }
    //      .flatMap(db.saveMany)
      .tapPrint(saved => s"Saved ${saved.size} landings")
      .map(_.map(toApiLanding))
      .flatMap(landings =>
        io.file.write(
          "/Users/palan/code/pescar/analytics.api/src/main/resources/landings-2010-2019-utf8.json",
          landings.map(_.asJson.noSpaces).mkString("[\n", ",\n", "]\n"),
        )
      )
      .tapPrintTimed("Total time: ")
      .as(ExitStatus.Success)
      //      .provideSomeLayer[ZEnv](dependencies)
      .orDie

  }

  val dependencies = db.doobie(Config.test.db) ++ httpClient.sttp

  def toApiLanding(landing: Landing): ApiLanding =
    landing match {
      case Landing(fecha, flota, puerto, _, _, _, _, lat, lon, _, especie, _, captura) =>
        val location = (lat zip lon).fold[Location](Miscellaneous(puerto))(geoLocation =>
          Harbour(puerto, GeoLocation.fromFloatPairUnsafe(geoLocation))
        )
        ApiLanding(fecha, location, Specie(especie), Fleet(flota), captura)
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
  def loadDataFromCsv(path: String) =
    io.file
      .list(path)
      .map(_.drop(1)) // Drop the header
      .map(lines => parse(lines, parser.parseLine))
      .flatMap(ZIO.fromEither(_).mapError(ParseFailure))

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
