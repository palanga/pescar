package gob.datos.consumer

import gob.datos.consumer.http.client
import gob.datos.consumer.http.client.HttpClient
import gob.datos.consumer.persistence.record.RecordPersistence
import zio.blocking.Blocking
import zio.console.Console
import zio.stream.ZStream
import zio.{ App, ZEnv, ZIO, ZManaged }

object Main extends App {

  override def run(args: List[String]): ZIO[ZEnv, Nothing, Int] =
    app.runDrain
      .as(ExitStatus.Success)
      .provideSomeManaged(dependencies)
      .orDie

  private val app = {

    import gob.datos.consumer.util.zio_syntax._

    val INITIAL_OFFSET = 0
    val PAGE_SIZE      = 100

    ZStream
      .iterate(INITIAL_OFFSET)(_ + PAGE_SIZE)
      .map(makeRequest(Constants.ResourceId.DESEMBARQUE_DE_CAPTURA_DE_ESPECIES_MARÍTIMAS_2019)(PAGE_SIZE))
      .mapMPar_(4, HttpClient.module.fetch)
      .map(_.result.records)
      .takeWhile(_.nonEmpty)
      .buffer(8)
      .mapM(RecordPersistence.module.saveMany)
      .tap(records => zio.console.putStrLn("Records saved:\n" ++ records.mkString("\n")))

  }

  private def makeRequest(resourceId: types.ResourceId)(pageSize: Int)(offset: Int) =
    client.types.Request(
      Constants.Url.DATOS_AGROINDUSTRIA_GOB_AR,
      types.RequestBody(pageSize, offset, resourceId),
    )

  object ExitStatus {
    val Success = 0
  }

  // TODO maybe we can use some other combinators to compose dependencies
  private val dependencies =
    for {
      env    <- ZManaged.environment[Blocking with Console]
      config <- thescientist.config.ConfigLoader.default.toManaged_
      sttp   <- http.client.SttpClient.makeManaged
      doobie <- persistence.record.DoobieRecordPersistence.makeManaged(config.db)
    } yield {
      new Blocking with Console with HttpClient with RecordPersistence {
        override val blocking          = env.blocking
        override val console           = env.console
        override val httpClient        = sttp.httpClient
        override val recordPersistence = doobie.recordPersistence
      }
    }

}
