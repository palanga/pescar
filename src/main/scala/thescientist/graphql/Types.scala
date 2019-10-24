package thescientist.graphql

import caliban.schema.Annotations.GQLDescription
import thescientist.Types.{ Metric, MetricTitle }
import zio.UIO

object Types {

  case class MetricsArgs(title: Option[MetricTitle])

  case class Queries(
    @GQLDescription("Return all metrics")
    metrics: MetricsArgs => UIO[List[Metric]],
  )

}
