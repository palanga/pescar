package api.database.landings

import java.time.YearMonth

import api.database.landings.landings_summary_tables._
import api.database.landings.landings_tables._
import api.types.{ Filter, Fleet, Location, Specie }
import zio.stream.ZStream

object Dummy {

  // TODO
  def landingsFromFilter(filter: Filter) =
    landingsByDate(filter.dates)
      .filter(
        landing =>
          (filter.locations contains landing.location.name) &&
            (filter.species contains landing.specie.name) &&
            (filter.fleets contains landing.fleet.name)
      )

  // TODO
  def landingsSummaryFromFilter(filter: Filter) =
    landingsFromFilter(filter).map(_.catchCount).fold(0)(_ + _)

  def landingsSummaryByDate(dates: Set[YearMonth]) =
    ZStream fromIterable landingsSummaryByDateTable(dates)

  def landingsSummaryByDateByLocation(dates: Set[YearMonth], locations: Set[Location]) =
    ZStream fromIterable landingsSummaryByDateByLocationTable(dates, locations)

  def landingsSummaryByDateBySpecie(dates: Set[YearMonth], species: Set[Specie]) =
    ZStream fromIterable landingsSummaryByDateBySpecieTable(dates, species)

  def landingsSummaryByDateByFleet(dates: Set[YearMonth], fleets: Set[Fleet]) =
    ZStream fromIterable landingsSummaryByDateByFleetTable(dates, fleets)

  def landingsByDate(dates: Set[YearMonth]) =
    ZStream fromIterable landingsByDateTable(dates)

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

  import api.types.Metric.Landing
  import landings_tables.{ landingsByDateByKeyTable, landingsByDateTable }
  import util.syntax.tupleops._

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

  import api.types.Metric.Landing

  /**
   *
   * create table landings_by_date (
   * ... Landing
   * primary key (date)
   * )
   *
   */
  def landingsByDateTable(dates: Set[YearMonth]) =
    all.filter(landing => dates contains landing.date)

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

  private val MarDelPlata  = Location.Harbour("Mar del Plata", util.GeoLocation.zero)
  private val PuertoMadryn = Location.Harbour("Puerto Madryn", util.GeoLocation.zero)

  val LOCATIONS_ALL = Set(MarDelPlata, PuertoMadryn)

  private val Langostino = Specie("Langostino")
  private val Pulpo      = Specie("Pulpo")

  val SPECIES_ALL = Set(Langostino, Pulpo)

  private val RadaORia   = Fleet("Rada o Ría")
  private val Fresqueros = Fleet("Fresqueros")

  val FLEETS_ALL = Set(RadaORia, Fresqueros)

  private val all = List(
    Landing(YearMonth.of(2017, 7), MarDelPlata, Langostino, RadaORia, 2),
    Landing(YearMonth.of(2017, 7), MarDelPlata, Pulpo, RadaORia, 3),
    Landing(YearMonth.of(2017, 7), PuertoMadryn, Langostino, RadaORia, 1),
    Landing(YearMonth.of(2017, 7), PuertoMadryn, Pulpo, Fresqueros, 1),
    Landing(YearMonth.of(2017, 8), MarDelPlata, Langostino, RadaORia, 10),
    Landing(YearMonth.of(2017, 8), MarDelPlata, Pulpo, RadaORia, 7),
    Landing(YearMonth.of(2017, 8), PuertoMadryn, Langostino, RadaORia, 9),
    Landing(YearMonth.of(2017, 8), PuertoMadryn, Pulpo, Fresqueros, 4),
  )

}
