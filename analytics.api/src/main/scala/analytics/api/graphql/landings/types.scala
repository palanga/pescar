package analytics.api.graphql.landings

import java.time.YearMonth

import analytics.api.types.Metric.{ Landing, LandingsSummary }
import analytics.api.types.{ FleetName, LocationName, SpecieName }
import zio.UIO
import zquery.ZQuery

object types {

  case class Queries(
    landings: Args => Node,
    locations: UIO[List[LocationName]],
    species: UIO[List[SpecieName]],
    fleets: UIO[List[FleetName]],
  )

  case class Args(
    dateRange: DateRange,
    locations: Option[List[LocationName]],
    species: Option[List[SpecieName]],
    fleets: Option[List[FleetName]],
  )

  case class Node(
    landings: ZQuery[Any, Nothing, List[Landing]],
    summary: ZQuery[Any, Nothing, LandingsSummary],
    byDate: UIO[Map[YearMonth, Node]],
    byLocation: UIO[Map[LocationName, Node]],
    bySpecie: UIO[Map[SpecieName, Node]],
    byFleet: UIO[Map[FleetName, Node]],
  )

  /**
   * @param from  inclusive
   * @param until exclusive
   */
  case class DateRange(from: YearMonth, until: YearMonth) {
    def toSet: Set[YearMonth] = {

      val fromYear   = from.getYear
      val fromMonth  = from.getMonthValue
      val untilYear  = until.getYear
      val untilMonth = until.getMonthValue

      def firstYearTruncated = (fromMonth to 12).map(month => YearMonth.of(fromYear, month))

      def lastYearTruncated = (1 until untilMonth).map(month => YearMonth.of(untilYear, month))

      def yearsBetween =
        for {
          year  <- (fromYear + 1) until untilYear
          month <- 1 to 12
        } yield YearMonth.of(year, month)

      if (from == until || from.isAfter(until)) {
        Set.empty
      } else if (fromYear == untilYear) {
        (fromMonth until untilMonth).map(month => YearMonth.of(fromYear, month)).toSet
      } else if (fromYear + 1 == untilYear) {
        firstYearTruncated.toSet ++ lastYearTruncated.toSet
      } else {
        firstYearTruncated.toSet ++ yearsBetween.toSet ++ lastYearTruncated.toSet
      }

    }
  }

}
