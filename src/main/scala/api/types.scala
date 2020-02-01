package api

import java.time.YearMonth

object types {

  sealed trait Metric extends Product with Serializable
  object Metric {

    final case class Landing(
      date: YearMonth,
      fleet: Fleet,
      location: Location,
      specie: Specie,
      fishCatch: Int,
    ) extends Metric

  }

  case class Fleet(name: String) extends AnyVal

  case class Location(port: Port, department: Department, province: Province)
  case class Port(name: String, geoLocation: Option[GeoLocation])
  case class GeoLocation(latitude: GeoDegree, longitude: GeoDegree)
  case class GeoDegree(value: Float)  extends AnyVal
  case class Department(name: String) extends AnyVal
  case class Province(name: String)   extends AnyVal

  case class Specie(name: String, category: Category, group: CategoryGroup)
  case class Category(name: String)      extends AnyVal
  case class CategoryGroup(name: String) extends AnyVal

}
