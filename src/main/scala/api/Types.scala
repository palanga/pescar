package api

object Types {

  case class Metric(title: MetricTitle, data: MetricData)

  type MetricTitle = String

  sealed trait MetricData
  case class Histogram(values: List[HistogramValue]) extends MetricData
  case class KPI(value: MetricValue)                 extends MetricData

  case class HistogramValue(month: Month, value: MetricValue)

  sealed trait Month
  case object January  extends Month
  case object February extends Month
  case object March    extends Month
  case object April    extends Month

  type MetricValue = Int

}
