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
    dates: List[YearMonth],
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

}
