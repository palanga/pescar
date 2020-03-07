package analytics.consumer.gob.datos.http.client

import analytics.consumer.gob.datos.http.client.types.Request
import analytics.consumer.gob.datos.types.ResponseBody
import zio.blocking.Blocking
import zio.console.Console
import zio.{ Has, ZIO, ZLayer }

object module {

  type HttpClient = Has[HttpClient.Service]

  object HttpClient {
    trait Service {
      def fetch(request: Request): ZIO[Blocking, Throwable, ResponseBody]
    }
  }

  def fetch(request: Request): ZIO[HttpClient with Blocking, Throwable, ResponseBody] =
    ZIO.accessM(_.get.fetch(request))

  val sttp: ZLayer[Console, Throwable, HttpClient]    = SttpClient.make
  val http4s: ZLayer[Blocking, Throwable, HttpClient] = Http4sClient.make

}
