package thescientist.graphql

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter._

import caliban.GraphQL.graphQL
import caliban.Value.StringValue
import caliban.schema.{ GenericSchema, Schema }
import caliban.{ GraphQL, RootResolver }
import thescientist.Data
import thescientist.Main.AppEnv
import thescientist.Types.{ Metric, MetricTitle }
import thescientist.graphql.Types.Queries
import zio.ZIO

object Interpreter extends GenericSchema[AppEnv] {

  type AppInterpreter = GraphQL[AppEnv, Queries, Nothing, Nothing, Throwable]

  // custom LocalDateTime schema typeclass instance
  implicit val localDateTimeSchema: Schema.Typeclass[LocalDateTime] =
    scalarSchema("LocalDateTime", None, localDateTime => StringValue(localDateTime format ISO_LOCAL_DATE_TIME))

  def apply: AppInterpreter = graphQL(
    RootResolver(
      Queries(
        args => ZIO succeed args.titleContains.fold(Data.metrics)(byTitle(Data.metrics)),
      ),
    )
  )

  private def byTitle(metrics: List[Metric])(titleWords: List[MetricTitle]): List[Metric] =
    metrics.filter(metric => titleWords exists metric.title.contains)

}
