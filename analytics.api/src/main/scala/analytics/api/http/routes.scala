package analytics.api.http

import analytics.api.Main.ZTask
import caliban.{ CalibanError, GraphQLInterpreter, Http4sAdapter }
import org.http4s.HttpApp
import org.http4s.dsl.Http4sDsl
import org.http4s.implicits._
import org.http4s.server.Router
import org.http4s.server.middleware.CORS
import zio.ZEnv
import zio.interop.catz._

object routes extends Http4sDsl[ZTask] {

  def withInterpreter(interpreter: GraphQLInterpreter[ZEnv, CalibanError]): HttpApp[ZTask] =
    Router(
      "/api" -> CORS(Http4sAdapter.makeHttpService(interpreter)),
      "/ws"  -> CORS(Http4sAdapter.makeWebSocketService(interpreter)),
    ).orNotFound

}
