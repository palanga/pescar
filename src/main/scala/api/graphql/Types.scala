package api.graphql

import caliban.schema.Annotations.GQLDescription
import api.Types.{ Metric, MetricTitle }
import api.metrics.Metrics
import zio.URIO

object Types {

  case class MetricsArgs(keywords: Option[List[MetricTitle]])

  case class Queries(
    @GQLDescription("Return all metrics")
    metrics: MetricsArgs => URIO[Metrics, List[Metric]],
  )

}
