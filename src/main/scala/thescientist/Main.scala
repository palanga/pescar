package thescientist

import org.http4s.server.blaze.BlazeServerBuilder
import thescientist.graphql.Interpreter
import thescientist.metrics.{ Metrics, MetricsMock }
import zio.clock.Clock
import zio.interop.catz._
import zio.{ App, RIO, ZEnv, ZIO }

object Main {

  trait BaseEnv extends Clock

  type AppEnv     = BaseEnv with Metrics
  type AppTask[A] = RIO[AppEnv, A]

  def run(args: List[String]): ZIO[ZEnv, Nothing, Int] =
    (makeServer.orDie map (_ => 0)).provideSome[ZEnv](
      env =>
        new MetricsMock with BaseEnv {
          override val clock = env.clock
      }
    )

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
