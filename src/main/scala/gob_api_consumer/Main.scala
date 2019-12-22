package gob_api_consumer

import gob_api_consumer.http.client.HttpClient
import zio.blocking.Blocking
import zio.console.Console
import zio.stream.ZStream
import zio.{ App, ZEnv, ZIO }

object Main extends App {

  override def run(args: List[String]): ZIO[ZEnv, Nothing, Int] =
    app
      .provideSomeManaged(dependencies)
      .runDrain
      .as(ExitStatus.Success)
      .orDie

  private val app = {

    import util.zio_syntax._

    val INITIAL_OFFSET = 2889
    val PAGE_SIZE      = 10

    ZStream
      .iterate(INITIAL_OFFSET)(_ + PAGE_SIZE)
      .map(makeRequest(Constants.ResourceId.DESEMBARQUE_DE_CAPTURA_DE_ESPECIES_MARÍTIMAS_2019)(PAGE_SIZE))
      .mapMPar_(4, HttpClient.module.fetch)
      .map(_.result.records)
      .takeWhile(_.nonEmpty)
      .buffer(8)
      .mapM(records => zio.console.putStrLn(records.mkString("\n")))

  }

  private val dependencies =
    ZIO.environment[Blocking with Console].toManaged_ >>= { env =>
      for {
        //        config       <- ConfigLoader.default.toManaged_
        //        transactor   <- Transactor.fromConfig(config.db)
        //        http4sClient <- http.Http4sClient.makeManaged
        sttpClient <- http.client.SttpClient.makeManaged
      } yield {
        new Blocking with Console with HttpClient {
          override val blocking   = env.blocking
          override val console    = env.console
          override val httpClient = sttpClient.httpClient
        }
      }
    }

  object ExitStatus {
    val Success = 0
  }

  private def makeRequest(resourceId: types.ResourceId)(pageSize: Int)(offset: Int) =
    http.client.types.Request(
      Constants.Url.DATOS_AGROINDUSTRIA_GOB_AR,
      types.RequestBody(pageSize, offset, resourceId),
    )

}
