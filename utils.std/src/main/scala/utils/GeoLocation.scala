package utils

import utils.syntax.either._

object GeoLocation {

  import GeoDegree.{ Latitude, Longitude }

  val zero: GeoLocation = GeoLocation(Latitude(0), Longitude(0))

  def fromPair(pair: (Latitude, Longitude)): GeoLocation =
    pair match {
      case (latitude, longitude) => GeoLocation(latitude, longitude)
    }

  def fromFloat(latitude: Float, longitude: Float): Either[IllegalArgumentException, GeoLocation] =
    (Latitude from latitude zipWithError (Longitude from longitude))(_ + _)
      .map(fromPair)

  def fromFloatUnsafe(latitude: Float, longitude: Float): GeoLocation =
    fromFloat(latitude, longitude).fold(throw _, identity)

  def fromFloatPair(pair: (Float, Float)): Either[IllegalArgumentException, GeoLocation] = fromFloat(pair._1, pair._2)

  def fromFloatPairUnsafe(pair: (Float, Float)): GeoLocation = fromFloatUnsafe(pair._1, pair._2)

  implicit class ExceptionOps[E <: Throwable](val self: E) extends AnyVal {
    def +(other: E): E = new Exception(self.getMessage ++ "\n" ++ other.getMessage).asInstanceOf[E]
  }

}

case class GeoLocation(latitude: GeoDegree.Latitude, longitude: GeoDegree.Longitude)

sealed trait GeoDegree extends Any with Product with Serializable
object GeoDegree {

  case class Latitude(value: Float)  extends AnyVal with GeoDegree
  case class Longitude(value: Float) extends AnyVal with GeoDegree

  private val LATITUDE_MIN  = -90
  private val LATITUDE_MAX  = 90
  private val LONGITUDE_MIN = -180
  private val LONGITUDE_MAX = 180

  object Latitude {
    def from(value: Float): Either[IllegalArgumentException, Latitude] =
      if (LATITUDE_MIN <= value && value <= LATITUDE_MAX) Right(Latitude(value))
      else Left(new IllegalArgumentException(s"Couldn't construct a Latitude from $value"))
  }

  object Longitude {
    def from(value: Float): Either[IllegalArgumentException, Longitude] =
      if (LONGITUDE_MIN <= value && value <= LONGITUDE_MAX) Right(Longitude(value))
      else Left(new IllegalArgumentException(s"Couldn't construct a Longitude from $value"))
  }

}
