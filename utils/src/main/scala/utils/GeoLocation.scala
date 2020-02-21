package utils

object GeoLocation {

  import GeoDegree.{ Latitude, Longitude }

  val zero = GeoLocation(Latitude(0), Longitude(0))

  def from(latitude: Float, longitude: Float): Either[String, GeoLocation] = ???

}

case class GeoLocation(latitude: GeoDegree.Latitude, longitude: GeoDegree.Longitude)

sealed trait GeoDegree extends Any with Product with Serializable
object GeoDegree {
  final case class Latitude(value: Float)  extends AnyVal with GeoDegree
  final case class Longitude(value: Float) extends AnyVal with GeoDegree
}
