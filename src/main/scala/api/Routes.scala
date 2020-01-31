package api

import caliban.{ CalibanError, GraphQLInterpreter, Http4sAdapter }
import org.http4s.dsl.Http4sDsl
import org.http4s.implicits._
import org.http4s.server.Router
import org.http4s.server.middleware.CORS
import api.Main.{ AppEnv, AppTask }
import zio.interop.catz._

object Routes extends Http4sDsl[AppTask] {

  def all(interpreter: GraphQLInterpreter[AppEnv, CalibanError]) =
    Router(
      "/api/graphql" -> CORS(Http4sAdapter.makeHttpService(interpreter)),
      "/ws/graphql"  -> CORS(Http4sAdapter.makeWebSocketService(interpreter)),
    ).orNotFound

}
