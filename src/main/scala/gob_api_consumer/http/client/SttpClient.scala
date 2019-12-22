package gob_api_consumer.http.client

import gob_api_consumer.http.client.types.{ DeserializationError, Request, UnsuccessfulResponse }
import gob_api_consumer.{ types, Constants }
import sttp.client.SttpBackend
import sttp.client.asynchttpclient.WebSocketHandler
import sttp.client.asynchttpclient.zio.AsyncHttpClientZioBackend
import zio.blocking.Blocking
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
        .post(uri"${Constants.Url.DATOS_AGROINDUSTRIA_GOB_AR}")
        .body(types.RequestBody(request.body.limit, request.body.offset, request.body.resource_id))
        .response(asJson[types.ResponseBody])
        .send()
        .flatMap(response => ZIO.fromEither(response.body).mapError(handleError(response)))

    }

  }

}

object SttpClient {

  val makeManaged =
    AsyncHttpClientZioBackend().toManaged(_.close.orDie) map { backend =>
      new SttpClient {
        override protected implicit val sttpBackend = backend
      }
    }

}
