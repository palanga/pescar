package gob_api_consumer.http.client

import gob_api_consumer.types.ResponseBody
import gob_api_consumer.http.client.types.Request
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
