package thescientist

import caliban.Http4sAdapter
import cats.data.Kleisli
import org.http4s.dsl.Http4sDsl
import org.http4s.implicits._
import org.http4s.server.Router
import org.http4s.server.middleware.CORS
import org.http4s.{ Request, Response }
import thescientist.Main.AppTask
import thescientist.graphql.Interpreter.AppInterpreter
import zio.interop.catz._

object Routes extends Http4sDsl[AppTask] {

  def all(interpreter: AppInterpreter): Kleisli[AppTask, Request[AppTask], Response[AppTask]] =
    Router(
      "/api/graphql" -> CORS(Http4sAdapter.makeRestService(interpreter)),
      "/ws/graphql"  -> CORS(Http4sAdapter.makeWebSocketService(interpreter)),
    ).orNotFound

}
