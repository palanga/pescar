package api.graphql.landings

import java.time.YearMonth

import api.types.Metric.{ Landing, LandingsSummary }
import api.types.{ FleetName, LocationName, SpecieName }
import zio.UIO
import zio.stream.Stream

object types {

  case class Queries(landings: Args => Node)

  case class Args(
    dates: List[YearMonth],
    locations: Option[List[LocationName]],
    species: Option[List[SpecieName]],
    fleets: Option[List[FleetName]],
  )

  case class Node(
    landings: Stream[Nothing, Landing] = Stream.empty,
    summary: UIO[LandingsSummary] = UIO effectTotal LandingsSummary(),
    byDate: UIO[Map[YearMonth, Node]] = UIO effectTotal Map.empty,
    byLocation: UIO[Map[LocationName, Node]] = UIO effectTotal Map.empty,
    bySpecie: UIO[Map[SpecieName, Node]] = UIO effectTotal Map.empty,
    byFleet: UIO[Map[FleetName, Node]] = UIO effectTotal Map.empty,
  )

}
