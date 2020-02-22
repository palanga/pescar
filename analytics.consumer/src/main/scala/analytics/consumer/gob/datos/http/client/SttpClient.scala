package analytics.consumer.gob.datos.http.client

import analytics.consumer.gob.datos.http.client.types.{ DeserializationError, Request, UnsuccessfulResponse }
import analytics.consumer.gob.datos.{ constants, types }
import sttp.client.SttpBackend
import sttp.client.asynchttpclient.WebSocketHandler
import sttp.client.asynchttpclient.zio.AsyncHttpClientZioBackend
import zio.blocking.Blocking
import zio.console.putStrLn
import zio.{ Task, ZIO }

trait SttpClient extends HttpClient {

  protected implicit val sttpBackend: SttpBackend[Task, Nothing, WebSocketHandler]

  override val httpClient = new HttpClient.Service[Blocking] {

    override def fetch(request: Request) = {

      import io.circe.generic.auto._
      import sttp.client.circe._
      import sttp.client.{ DeserializationError => SttpDeserializationError, _ }

      def handleError(response: Response[_])(error: ResponseError[_]) = error match {
        case HttpError(body) =>
          UnsuccessfulResponse(s"${response.code} ${response.statusText}: $body")
        case SttpDeserializationError(original, error) =>
          DeserializationError(s"${error.toString} - Response body: $original")
      }

      basicRequest
        .post(uri"${constants.Url.DATOS_AGROINDUSTRIA_GOB_AR}")
        .body(types.RequestBody(request.body.limit, request.body.offset, request.body.resource_id))
        .response(asJson[types.ResponseBody])
        .send()
        .flatMap(response => ZIO.fromEither(response.body).mapError(handleError(response)))

    }

  }

}

object SttpClient {

  val makeManaged =
    AsyncHttpClientZioBackend().toManaged(closeBackend) map { backend =>
      new SttpClient {
        override protected implicit val sttpBackend = backend
      }
    }

  private def closeBackend(backend: SttpBackend[Task, Nothing, WebSocketHandler]) =
    putStrLn("Closing sttp backend...") *> backend.close.orDie <* putStrLn("Succesfully closed sttp backend.")

}
