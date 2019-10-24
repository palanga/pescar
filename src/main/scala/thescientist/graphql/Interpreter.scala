package thescientist.graphql

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter._

import caliban.GraphQL.graphQL
import caliban.ResponseValue.StringValue
import caliban.schema.{ GenericSchema, Schema }
import caliban.{ GraphQL, RootResolver }
import thescientist.Data
import thescientist.Main.AppEnv
import thescientist.graphql.Types.Queries
import zio.ZIO

object Interpreter extends GenericSchema[AppEnv] {

  type AppInterpreter = GraphQL[AppEnv, Queries, Nothing, Nothing]

  // custom LocalDateTime schema typeclass instance
  implicit val localDateTimeSchema: Schema.Typeclass[LocalDateTime] =
    scalarSchema("LocalDateTime", None, localDateTime => StringValue(localDateTime format ISO_LOCAL_DATE_TIME))

  def apply: AppInterpreter = graphQL(
    RootResolver(
      Queries(
        args => ZIO succeed Data.metrics.filter(metric => metric.title.contains(args.title.getOrElse(metric.title))),
      ),
    )
  )

}
