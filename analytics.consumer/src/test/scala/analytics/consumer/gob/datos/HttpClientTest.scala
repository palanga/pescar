package analytics.consumer.gob.datos

import zio.test._
import zio.test.environment.TestEnvironment

// TODO ?
object HttpClientTest extends DefaultRunnableSpec {

  private val test = Main.streamDataFromApi(0, 1).take(3).runDrain.as(assert(())(Assertion.anything))

  private val sttpClient   = http.client.module.sttp.mapError(TestFailure.fail)
  private val http4sClient = http.client.module.http4s.mapError(TestFailure.fail)

  override def spec =
    suite("analytics http client datos.gob")(
      testM("sttp works")(test).provideSomeLayer[TestEnvironment](sttpClient),
      testM("http4s works")(test).provideSomeLayer[TestEnvironment](http4sClient),
    )

}
