package thescientist.metrics

import thescientist.Types.Metric
import zio.ZIO

trait Metrics {
  val metrics: Metrics.Service[Any]
}

object Metrics {
  trait Service[R] {
    def all: ZIO[R, Nothing, List[Metric]]
    def filterByTitle(keywords: List[String]): ZIO[R, Nothing, List[Metric]]
  }
}

trait MetricsMock extends Metrics {

  import thescientist.persistence.Data

  final val metrics = new Metrics.Service[Any] {
    override def all: ZIO[Any, Nothing, List[Metric]] = ZIO succeed Data.metrics
    override def filterByTitle(keywords: List[String]): ZIO[Any, Nothing, List[Metric]] =
      ZIO succeed Data.metrics.filter(metric => keywords exists metric.title.contains)
  }

}

object MetricsMock extends MetricsMock
