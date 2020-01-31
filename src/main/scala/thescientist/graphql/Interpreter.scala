package thescientist.graphql

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter._

import caliban.GraphQL.graphQL
import caliban.RootResolver
import caliban.Value.StringValue
import caliban.schema.{ GenericSchema, Schema }
import thescientist.Main.AppEnv
import thescientist.graphql.Types.Queries
import thescientist.metrics.Metrics

object Interpreter extends GenericSchema[AppEnv] {

  // custom LocalDateTime schema typeclass instance
  implicit val localDateTimeSchema: Schema.Typeclass[LocalDateTime] =
    scalarSchema("LocalDateTime", None, localDateTime => StringValue(localDateTime format ISO_LOCAL_DATE_TIME))

  val make = graphQL(
    RootResolver(
      Queries(
        _.keywords.fold(Metrics.>.all)(Metrics.>.filterByTitle),
      ),
    )
  )

}
