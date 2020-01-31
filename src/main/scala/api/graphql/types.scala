package api.graphql

import api.metrics.Metrics
import api.types.{ Metric, MetricTitle }
import caliban.schema.Annotations.GQLDescription
import zio.URIO

object types {

  case class MetricsArgs(keywords: Option[List[MetricTitle]])

  case class Queries(
    @GQLDescription("Return all metrics")
    metrics: MetricsArgs => URIO[Metrics, List[Metric]],
  )

}
