package thescientist

import org.http4s.server.blaze.BlazeServerBuilder
import thescientist.graphql.Interpreter
import zio.interop.catz._
import zio.{ App, RIO, ZEnv, ZIO }

object Main extends App {

  type AppEnv     = ZEnv
  type AppTask[A] = RIO[AppEnv, A]

  override def run(args: List[String]): ZIO[AppEnv, Nothing, Int] =
    makeServer.orDie map (_ => 0)

  val httpApp = Routes all Interpreter.apply

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
