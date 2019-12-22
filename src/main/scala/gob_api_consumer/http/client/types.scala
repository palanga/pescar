package gob_api_consumer.http.client

import gob_api_consumer.types.RequestBody
import zio.RIO
import zio.blocking.Blocking

object types {

  case class Request(
    uri: String,
    body: RequestBody,
  )

  type BlockingIO[A] = RIO[Blocking, A]

  case class UnsuccessfulResponse(message: String) extends Exception(message)
  case class DeserializationError(message: String) extends Exception(message)

}
