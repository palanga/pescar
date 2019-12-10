package thescientist.graphql

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter._

import caliban.GraphQL.graphQL
import caliban.Value.StringValue
import caliban.schema.{ GenericSchema, Schema }
import caliban.{ GraphQL, RootResolver }
import thescientist.Main.AppEnv
import thescientist.graphql.Types.Queries
import thescientist.metrics.MetricsMock.metrics

object Interpreter extends GenericSchema[AppEnv] {

  type AppInterpreter = GraphQL[AppEnv, Queries, Nothing, Nothing, Throwable]

  // custom LocalDateTime schema typeclass instance
  implicit val localDateTimeSchema: Schema.Typeclass[LocalDateTime] =
    scalarSchema("LocalDateTime", None, localDateTime => StringValue(localDateTime format ISO_LOCAL_DATE_TIME))

  def apply: AppInterpreter = graphQL(
    RootResolver(
      Queries(
        _.keywords.fold(metrics.all)(metrics.filterByTitle),
      ),
    )
  )

}
