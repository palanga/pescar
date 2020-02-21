package api

import api.graphql.landings.schema
import api.http.routes
import org.http4s.server.blaze.BlazeServerBuilder
import zio.clock.Clock
import zio.interop.catz._
import zio.{ App, RIO, ZEnv, ZIO }

object Main extends App {

  trait BaseEnv extends Clock

  type AppEnv     = Clock
  type AppTask[A] = RIO[AppEnv, A]

  override def run(args: List[String]): ZIO[ZEnv, Nothing, Int] =
    zio.console.putStrLn(api.render) *> makeServer.orDie map (_ => 0)

  val api     = schema.make
  val httpApp = routes withInterpreter api.interpreter

  private val makeServer: ZIO[AppEnv, Throwable, Unit] =
    ZIO.runtime[AppEnv] >>= { implicit env =>
      BlazeServerBuilder[AppTask]
        .bindHttp(8080, "localhost")
        .withHttpApp(httpApp)
        .serve
        .compile
        .drain
    }

}
