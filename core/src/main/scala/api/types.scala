package api

import java.time.YearMonth

import util.GeoLocation

object types {

  sealed trait Metric extends Product with Serializable
  object Metric {

    final case class Landing(
      date: YearMonth,
      location: Location,
      specie: Specie,
      fleet: Fleet,
      catchCount: Int,
    ) extends Metric

    final case class LandingsSummary(
      total: Int,
    ) extends Metric

  }

  sealed trait Location extends Any with Product with Serializable {
    def name: String
  }
  object Location {
    final case class Harbour(name: String, geoLocation: GeoLocation) extends Location
    final case class Miscellaneous(name: String)                     extends AnyVal with Location
  }

  case class Specie(name: String) extends AnyVal

  case class Fleet(name: String) extends AnyVal

  case class Filter(
    dates: Set[YearMonth],
    locations: Set[LocationName],
    species: Set[SpecieName],
    fleets: Set[FleetName],
  )

  type LocationName = String
  type SpecieName   = String
  type FleetName    = String

}
