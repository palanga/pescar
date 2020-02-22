package analytics.consumer.gob.datos.http.client

import analytics.consumer.gob.datos.http.client.types.{ BlockingIO, Request, UnsuccessfulResponse }
import analytics.consumer.gob.datos.types.ResponseBody
import org.http4s.client.Client
import org.http4s.client.blaze.BlazeClientBuilder
import org.http4s.{ Method, Response, Request => Http4sRequest }
import zio.ZIO
import zio.blocking.Blocking

trait Http4sClient extends HttpClient {

  protected val httpClientBackend: Client[BlockingIO]

  override val httpClient = new HttpClient.Service[Blocking] {

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

}

object Http4sClient {

  import zio.interop.catz._

  val makeManaged =
    ZIO.runtime[Blocking].toManaged_ flatMap { implicit runtime =>
      BlazeClientBuilder[BlockingIO](runtime.platform.executor.asEC).resource.toManaged
    } map { backend =>
      new Http4sClient {
        override protected val httpClientBackend: Client[BlockingIO] = backend
      }
    }

}
