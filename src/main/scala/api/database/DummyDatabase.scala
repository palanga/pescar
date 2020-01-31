package api.database

import api.types._

object DummyDatabase {

  private val months = List(
    January,
    February,
    March,
    April
  )

  private val numbers = List(
    List(190, 23, 347, 1234),
    List(234, 12, 234, 3267),
    List(345, 65, 846, 2378),
    List(934, 92, 234, 599)
  )

  private val titles = List(
    "Desembarques de langostinos por mes",
    "Desembarques de merluza por mes",
    "Desembarques de pejerrey por mes",
    "Desembarques de pulpo por mes"
  )

  val metrics: List[Metric] =
    numbers
      .map(months zip _)
      .map(_ map { case (month, value) => HistogramValue(month, value) })
      .map(Histogram)
      .zip(titles)
      .map { case (metricData, title) => Metric(title, metricData) }

}
