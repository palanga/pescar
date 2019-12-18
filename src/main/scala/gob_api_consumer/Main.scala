package gob_api_consumer

import gob_api_consumer.Types.{ Record, RequestBody, ResponseBody }
import io.circe.generic.auto._
import sttp.client._
import sttp.client.asynchttpclient.zio.AsyncHttpClientZioBackend
import sttp.client.circe._
import zio.console.putStrLn
import zio.stream.Stream
import zio.{ App, ZEnv, ZIO }

object Main extends App {

  override def run(args: List[String]): ZIO[ZEnv, Nothing, Int] = app.orDie.map(_ => 0)

  private val sttpBackend = AsyncHttpClientZioBackend()

  private val INITIAL_OFFSET = 2800
  private val PAGE_SIZE      = 100

  private val app = sttpBackend >>= { implicit backend =>
    Stream
      .iterate(INITIAL_OFFSET)(_ + PAGE_SIZE)
      .map(offset => makeRequest(offset).send() <* putStrLn(s"CURRENT OFFSET: $offset"))
      .map(_ >>= save)
      .foreachWhile(_ map hasNext)
  }

  private def makeRequest(offset: Int) =
    quickRequest
      .post(uri"${Constants.Url.DATOS_AGROINDUSTRIA_GOB_AR}")
      .body(RequestBody(PAGE_SIZE, offset, Constants.ResourceId.DESEMBARQUE_DE_CAPTURA_DE_ESPECIES_MARÍTIMAS_2019))
      .response(asJson[ResponseBody])

  // TODO actually save
  private def save(response: Response[Either[ResponseError[io.circe.Error], ResponseBody]]) =
    for {
      body <- ZIO fromEither response.body
      _    <- putStrLn(body.result.records.mkString("\n"))
    } yield body.result.records

  private def hasNext(list: List[Record]) = list.nonEmpty

}
