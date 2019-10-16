package thescientist

import java.util.concurrent.Executors

import caliban.GraphQL
import caliban.schema.Annotations.{GQLDeprecated, GQLDescription}
import cats.effect.Blocker
import org.http4s.implicits._
import org.http4s.server.Router
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.server.middleware.CORS
import thescientist.ExampleData._
import zio.clock.Clock
import zio.console.Console
import zio.interop.catz._
import zio.stream.ZStream
import zio.{App, RIO, URIO, ZIO}

object Main extends App {

  case class Queries(
                      @GQLDescription("Return all characters from a given origin")
                      characters: CharactersArgs => URIO[Console, List[Character]],
                      @GQLDeprecated("Use `characters`")
                      character: CharacterArgs => URIO[Console, Option[Character]]
                    )

  case class Mutations(deleteCharacter: CharacterArgs => URIO[Console, Boolean])

  case class Subscriptions(characterDeleted: ZStream[Console, Nothing, String])

  type ExampleTask[A] = RIO[Console with Clock, A]

  private val blockingPool = Executors.newFixedThreadPool(4)
  private val blocker = Blocker.liftExecutorService(blockingPool)

  private def server(interpreter: GraphQL[Console with Clock, Queries, Mutations, Subscriptions]): ZIO[Environment, Throwable, Unit] =
    ZIO.runtime[Environment] >>= { implicit env =>
      BlazeServerBuilder[ExampleTask]
        .bindHttp(8080, "localhost")
        .withHttpApp(
          Router(
            "/api/graphql" -> CORS(caliban.Http4sAdapter.makeRestService(interpreter)),
            "/ws/graphql" -> CORS(caliban.Http4sAdapter.makeWebSocketService(interpreter)),
            "/" -> Routes.graphiql(blocker)
          ).orNotFound
        )
        .serve
        .compile
        .drain
    }

  override def run(args: List[String]): ZIO[Environment, Nothing, Int] =
    (for {
      service <- ExampleService.make(sampleCharacters)
      program <- server(Interpreter.interpreter(service))
    } yield program).orDie.map(_ => 0)
}
