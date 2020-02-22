package gob.datos.consumer.http.client

import gob.datos.consumer.http.client.types.Request
import gob.datos.consumer.types.ResponseBody
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
