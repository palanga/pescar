package api.http

import api.Main.{ AppEnv, AppTask }
import caliban.{ CalibanError, GraphQLInterpreter, Http4sAdapter }
import org.http4s.dsl.Http4sDsl
import org.http4s.implicits._
import org.http4s.server.Router
import org.http4s.server.middleware.CORS
import zio.interop.catz._

object routes extends Http4sDsl[AppTask] {

  def withInterpreter(interpreter: GraphQLInterpreter[AppEnv, CalibanError]) =
    Router(
      "/api" -> CORS(Http4sAdapter.makeHttpService(interpreter)),
      "/ws"  -> CORS(Http4sAdapter.makeWebSocketService(interpreter)),
    ).orNotFound

}
