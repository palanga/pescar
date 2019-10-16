package thescientist

import cats.effect.Blocker
import org.http4s.dsl.Http4sDsl
import org.http4s.{HttpRoutes, StaticFile}
import thescientist.Main.ExampleTask
import zio.interop.catz._

object Routes extends Http4sDsl[ExampleTask] {

  def graphiql(blocker: Blocker): HttpRoutes[ExampleTask] = HttpRoutes.of[ExampleTask] {
    case request@GET -> Root / "graphiql" =>
      StaticFile.fromResource("/graphiql.html", blocker, Some(request)).getOrElseF(NotFound())
  }

}
