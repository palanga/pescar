package thescientist.graphql

import caliban.schema.Annotations.GQLDescription
import thescientist.Types.{ Metric, MetricTitle }
import thescientist.metrics.Metrics
import zio.URIO

object Types {

  case class MetricsArgs(keywords: Option[List[MetricTitle]])

  case class Queries(
    @GQLDescription("Return all metrics")
    metrics: MetricsArgs => URIO[Metrics, List[Metric]],
  )

}
