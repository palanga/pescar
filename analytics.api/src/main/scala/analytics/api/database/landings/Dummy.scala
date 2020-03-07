package analytics.api.database.landings

import java.time.YearMonth

import analytics.api.database.landings.landings_summary_tables._
import analytics.api.database.landings.landings_tables._
import analytics.api.database.landings.module.LandingsDatabase
import analytics.api.types.Location.{ Harbour, Miscellaneous }
import analytics.api.types.{ Filter, Fleet, Location, Specie }
import utils.GeoLocation
import zio.stream.ZStream

/**
 * TODO the whole file
 */
object Dummy extends LandingsDatabase.Service {

  // TODO
  override def landingsFromFilter(filter: Filter) = {

    import analytics.api.types.Metric.Landing
    import analytics.consumer.gob.datos.database.landing.{ module => db }
    import analytics.consumer.gob.datos.types.{ Landing => ConsumerLanding }
    import config.Config

    def toApiLanding(landing: ConsumerLanding): Landing = landing match {
      case ConsumerLanding(fecha, flota, puerto, _, _, _, _, lat, lon, _, especie, _, captura) =>
        val location = (lat zip lon).fold[Location](Miscellaneous(puerto))(
          geoLocation => Harbour(puerto, GeoLocation.fromFloatPairUnsafe(geoLocation))
        )
        Landing(fecha, location, Specie(especie), Fleet(flota), captura)
    }

    // TODO
    db
      .find(filter.dates)
      .map(toApiLanding)
      .filter(
        landing =>
          (filter.locations contains landing.location.name) &&
          (filter.species contains landing.specie.name) &&
          (filter.fleets contains landing.fleet.name)
      )
      .provideSomeLayer(db.doobie(Config.test.db))

  }

  // TODO
  override def landingsSummaryFromFilter(filter: Filter) =
    landingsFromFilter(filter).map(_.catchCount).fold(0)(_ + _)

  def landingsSummaryByDate(dates: Set[YearMonth]) =
    ZStream fromIterable landingsSummaryByDateTable(dates)

  def landingsSummaryByDateByLocation(dates: Set[YearMonth], locations: Set[Location]) =
    ZStream fromIterable landingsSummaryByDateByLocationTable(dates, locations)

  def landingsSummaryByDateBySpecie(dates: Set[YearMonth], species: Set[Specie]) =
    ZStream fromIterable landingsSummaryByDateBySpecieTable(dates, species)

  def landingsSummaryByDateByFleet(dates: Set[YearMonth], fleets: Set[Fleet]) =
    ZStream fromIterable landingsSummaryByDateByFleetTable(dates, fleets)

  def landingsByDateByLocation(dates: Set[YearMonth], locations: Set[Location]) =
    ZStream fromIterable landingsByDateByLocationTable(dates, locations)
      .map(landing => (landing.date, landing.location, landing))

  def landingsByDateBySpecie(dates: Set[YearMonth], species: Set[Specie]) =
    ZStream fromIterable landingsByDateBySpecieTable(dates, species)
      .map(landing => (landing.date, landing.specie, landing))

  def landingsByDateByFleet(dates: Set[YearMonth], fleets: Set[Fleet]) =
    ZStream fromIterable landingsByDateByFleetTable(dates, fleets)
      .map(landing => (landing.date, landing.fleet, landing))

}

object landings_summary_tables {

  import analytics.api.types.Metric.Landing
  import landings_tables.{ landingsByDateByKeyTable, landingsByDateTable }
  import utils.syntax.tuple._

  /**
   * date | total
   * -----+------
   */
  def landingsSummaryByDateTable(dates: Set[YearMonth]) =
    landingsByDateTable(dates).groupMapReduce(_.date)(_.catchCount)(_ + _).toList

  /**
   * date | location | total
   * -----+----------+------
   */
  def landingsSummaryByDateByLocationTable(dates: Set[YearMonth], locations: Set[Location]) =
    landingsSummaryByDateByKeyTable(dates, locations, _.location)

  /**
   * date | specie | total
   * -----+--------+------
   */
  def landingsSummaryByDateBySpecieTable(dates: Set[YearMonth], species: Set[Specie]) =
    landingsSummaryByDateByKeyTable(dates, species, _.specie)

  /**
   * date | fleet | total
   * -----+-------+------
   */
  def landingsSummaryByDateByFleetTable(dates: Set[YearMonth], fleets: Set[Fleet]) =
    landingsSummaryByDateByKeyTable(dates, fleets, _.fleet)

  private def landingsSummaryByDateByKeyTable[K](dates: Set[YearMonth], ks: Set[K], getKey: Landing => K) =
    landingsByDateByKeyTable(dates, ks, getKey)
      .groupMapReduce(landing => (landing.date, getKey(landing)))(_.catchCount)(_ + _)
      .toList
      .map(_.flatten)

}

object landings_tables {

  import analytics.api.types.Metric.Landing

  /**
   *
   * create table landings_by_date (
   * ... Landing
   * primary key (date)
   * )
   *
   */
  def landingsByDateTable(dates: Set[YearMonth]): Seq[Landing] = ???

  /**
   *
   * create table landings_by_date_by_location (
   * ... Landing
   * primary key (date, location)
   * )
   *
   */
  def landingsByDateByLocationTable(dates: Set[YearMonth], locations: Set[Location]) =
    landingsByDateByKeyTable(dates, locations, _.location)

  /**
   *
   * create table landings_by_date_by_specie (
   * ... Landing
   * primary key (date, specie)
   * )
   *
   */
  def landingsByDateBySpecieTable(dates: Set[YearMonth], species: Set[Specie]) =
    landingsByDateByKeyTable(dates, species, _.specie)

  /**
   *
   * create table landings_by_date_by_fleet (
   * ... Landing
   * primary key (date, fleet)
   * )
   *
   */
  def landingsByDateByFleetTable(dates: Set[YearMonth], fleets: Set[Fleet]) =
    landingsByDateByKeyTable(dates, fleets, _.fleet)

  def landingsByDateByKeyTable[K](dates: Set[YearMonth], ks: Set[K], getKey: Landing => K) =
    landingsByDateTable(dates).filter(landing => ks contains getKey(landing))

}
