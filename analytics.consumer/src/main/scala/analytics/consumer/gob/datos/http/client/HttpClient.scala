package analytics.consumer.gob.datos.http.client

import analytics.consumer.gob.datos.http.client.types.Request
import analytics.consumer.gob.datos.types.ResponseBody
import zio.ZIO
import zio.blocking.Blocking

trait HttpClient {
  val httpClient: HttpClient.Service[Blocking]
}

object HttpClient {

  trait Service[R] {
    def fetch(request: Request): ZIO[R, Throwable, ResponseBody]
  }

  object module extends Service[HttpClient with Blocking] {
    override def fetch(request: Request) = ZIO.accessM(_.httpClient.fetch(request))
  }

}
