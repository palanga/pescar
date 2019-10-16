package thescientist

import caliban.GraphQL
import caliban.GraphQL.graphQL
import caliban.schema.GenericSchema
import thescientist.Main.{Mutations, Queries, Subscriptions}
import zio.clock.Clock
import zio.console.Console

object Interpreter {

  object dsl extends GenericSchema[Console with Clock]
  import dsl._

  def interpreter(service: ExampleService): GraphQL[Console with Clock, Queries, Mutations, Subscriptions] = graphQL(
    caliban.RootResolver(
      Queries(args => service.getCharacters(args.origin), args => service.findCharacter(args.name)),
      Mutations(args => service.deleteCharacter(args.name)),
      Subscriptions(service.deletedEvents)
    )
  )

}
