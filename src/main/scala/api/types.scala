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
      catchCount: Int = 0,
    ) extends Metric

    final case class LandingsSummary(
      total: Int = 0,
      byLocation: Map[Location, LandingsSummary] = Map.empty,
      bySpecie: Map[Specie, LandingsSummary] = Map.empty,
      byFleet: Map[Fleet, LandingsSummary] = Map.empty,
    ) extends Metric

  }

  sealed trait Location extends Any with Product with Serializable
  object Location {
    final case class Harbour(name: String, geoLocation: GeoLocation) extends Location
    final case class Miscellaneous(name: String)                     extends AnyVal with Location
  }

  case class Specie(name: String) extends AnyVal

  case class Fleet(name: String) extends AnyVal

}
