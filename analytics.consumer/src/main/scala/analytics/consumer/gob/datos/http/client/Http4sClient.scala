package analytics.consumer.gob.datos.http.client

import analytics.consumer.gob.datos.http.client.module.HttpClient
import analytics.consumer.gob.datos.http.client.types.{ BlockingIO, Request, UnsuccessfulResponse }
import analytics.consumer.gob.datos.types.ResponseBody
import org.http4s.client.Client
import org.http4s.client.blaze.BlazeClientBuilder
import org.http4s.{ Method, Response, Request => Http4sRequest }
import zio.blocking.Blocking
import zio.{ ZIO, ZLayer }

object Http4sClient {

  import zio.interop.catz._

  val make: ZLayer[Blocking, Throwable, HttpClient] =
    ZIO
      .runtime[Blocking]
      .toManaged_
      .flatMap { implicit runtime =>
        BlazeClientBuilder[BlockingIO](runtime.platform.executor.asEC).resource.toManaged
      }
      .map(new Http4sClient(_))
      .toLayer

}

private final class Http4sClient(httpClientBackend: Client[BlockingIO]) extends module.HttpClient.Service {

  override def fetch(request: Request) = {

    import io.circe.generic.auto._
    import org.http4s.circe.CirceEntityCodec._
    import zio.interop.catz._

    // This "succeeded error" will be mapped to the zio's error channel. That's the way http4s works 🤷🏻‍
    def handleError(response: Response[BlockingIO]) =
      for {
        body   <- response.bodyAsText.compile.string
        status = response.status
      } yield UnsuccessfulResponse(s"$status: $body")

    for {
      uri           <- ZIO.fromEither(org.http4s.Uri.fromString(request.uri))
      http4sRequest = Http4sRequest[BlockingIO](Method.POST, uri).withEntity(request.body)
      response      <- httpClientBackend.expectOr[ResponseBody](http4sRequest)(handleError)
    } yield response

  }

}
