package thescientist

import caliban.Http4sAdapter
import cats.data.Kleisli
import cats.effect.Blocker
import org.http4s.dsl.Http4sDsl
import org.http4s.implicits._
import org.http4s.server.Router
import org.http4s.server.middleware.CORS
import org.http4s.{ HttpRoutes, Request, Response, StaticFile }
import thescientist.Main.{ AppEnv, AppTask }
import thescientist.graphql.Interpreter.AppInterpreter
import zio.ZIO
import zio.interop.catz._

object Routes extends Http4sDsl[AppTask] {

  def all(interpreter: AppInterpreter): Kleisli[AppTask, Request[AppTask], Response[AppTask]] =
    Router(
      "/api/graphql" -> CORS(Http4sAdapter.makeRestService(interpreter)),
      "/ws/graphql"  -> CORS(Http4sAdapter.makeWebSocketService(interpreter)),
      "/graphiql"    -> graphiql,
    ).orNotFound

  private def graphiql: HttpRoutes[AppTask] =
    HttpRoutes.of[AppTask] {
      case GET -> Root => getResourceFile("/graphiql.html")
    }

  private def getResourceFile(fileName: String) =
    ZIO.runtime[AppEnv] >>= { implicit env =>
      StaticFile
        .fromResource(fileName, Blocker liftExecutionContext env.Platform.executor.asEC, None)
        .getOrElseF(NotFound())
    }

}
