package api.graphql

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter._

import api.Main.AppEnv
import api.graphql.types.Queries
import api.metrics.Metrics
import caliban.GraphQL.graphQL
import caliban.Value.StringValue
import caliban.schema.{ GenericSchema, Schema }
import caliban.{ CalibanError, GraphQL, GraphQLInterpreter, RootResolver }

object Interpreter extends GenericSchema[AppEnv] {

  // custom LocalDateTime schema typeclass instance
  implicit val localDateTimeSchema: Schema.Typeclass[LocalDateTime] =
    scalarSchema("LocalDateTime", None, localDateTime => StringValue(localDateTime format ISO_LOCAL_DATE_TIME))

  private val gql: GraphQL[AppEnv] = graphQL(
    RootResolver(
      Queries(
        _.keywords.fold(Metrics.>.all)(Metrics.>.filterByTitle),
      ),
    )
  )

  val make: GraphQLInterpreter[AppEnv, CalibanError] = gql.interpreter

}
