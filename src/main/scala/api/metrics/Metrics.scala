package api.metrics

import api.types.Metric
import zio.ZIO

trait Metrics {
  val metrics: Metrics.Service[Any]
}

object Metrics {

  trait Service[R] {
    def all: ZIO[R, Nothing, List[Metric]]
    def filterByTitle(keywords: List[String]): ZIO[R, Nothing, List[Metric]]
  }

  object > extends Metrics.Service[Metrics] {
    override def all: ZIO[Metrics, Nothing, List[Metric]] = ZIO accessM (_.metrics.all)
    override def filterByTitle(keywords: List[String]): ZIO[Metrics, Nothing, List[Metric]] =
      ZIO accessM (_.metrics filterByTitle keywords)
  }

}

trait MetricsMock extends Metrics {

  import api.database.DummyDatabase

  final val metrics = new Metrics.Service[Any] {
    override def all: ZIO[Any, Nothing, List[Metric]] = ZIO succeed DummyDatabase.metrics
    override def filterByTitle(keywords: List[String]): ZIO[Any, Nothing, List[Metric]] =
      ZIO succeed DummyDatabase.metrics.filter(metric => keywords exists metric.title.contains)
  }

}

object MetricsMock extends MetricsMock
