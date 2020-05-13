package analytics.api

import analytics.api.database.landings.module.LandingsDatabase
import analytics.api.database.landings.{ module => db }
import analytics.api.graphql.landings.schema
import analytics.api.http.routes
import org.http4s.server.blaze.BlazeServerBuilder
import zio.console.putStrLn
import zio.interop.catz.CatsApp
import zio.{ RIO, ZEnv, ZIO }

object Main extends CatsApp {

  type AppEnv   = ZEnv with LandingsDatabase
  type ZTask[A] = RIO[ZEnv, A]

  override def run(args: List[String]): ZIO[ZEnv, Nothing, Int] =
    putStrLn(api.render) *> startServer.orDie as 0

  val api     = schema.make
  val httpApp =
    api.interpreter
      .map(_.provideSomeLayer[ZEnv](db. inMemory.orDie))
      .map(routes.withInterpreter)

  private val startServer = {
    import zio.interop.catz.{ taskEffectInstance, zioTimer }
    httpApp.flatMap { httpApp =>
      BlazeServerBuilder[ZTask]
        .bindHttp(8080, "localhost")
        .withHttpApp(httpApp)
        .serve
        .compile
        .drain
    }
  }

}
