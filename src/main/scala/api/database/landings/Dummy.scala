package api.database.landings

import java.time.YearMonth

import api.database.landings.landings_summary_tables._
import api.database.landings.landings_tables._
import api.types.{ Fleet, Location, Specie }
import util.syntax.listops._
import zio.ZIO

object Dummy {

  def landingsSummaryByDate(dates: Set[YearMonth]) =
    ZIO succeed landingsSummaryByDateTable(dates).toMap

  def landingsSummaryByDateByLocation(dates: Set[YearMonth], locations: Set[Location]) =
    ZIO succeed landingsSummaryByDateByLocationTable(dates, locations).toMultiMap

  def landingsSummaryByDateBySpecie(dates: Set[YearMonth], species: Set[Specie]) =
    ZIO succeed landingsSummaryByDateBySpecieTable(dates, species).toMultiMap

  def landingsSummaryByDateByFleet(dates: Set[YearMonth], fleets: Set[Fleet]) =
    ZIO succeed landingsSummaryByDateByFleetTable(dates, fleets).toMultiMap

  def landingsByDate(dates: Set[YearMonth]) =
    ZIO succeed landingsByDateTable(dates).map(landing => (landing.date, landing)).toMap

  def landingsByDateByLocation(dates: Set[YearMonth], locations: Set[Location]) =
    ZIO succeed landingsByDateByLocationTable(dates, locations)
      .map(landing => (landing.date, landing.location, landing))
      .toMultiMap

  def landingsByDateBySpecie(dates: Set[YearMonth], species: Set[Specie]) =
    ZIO succeed landingsByDateBySpecieTable(dates, species)
      .map(landing => (landing.date, landing.specie, landing))
      .toMultiMap

  def landingsByDateByFleet(dates: Set[YearMonth], fleets: Set[Fleet]) =
    ZIO succeed landingsByDateByFleetTable(dates, fleets)
      .map(landing => (landing.date, landing.fleet, landing))
      .toMultiMap

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

  private val Langostino = Specie("Langostino")
  private val Pulpo      = Specie("Pulpo")

  private val emptyFleet = Fleet("")

  private val all = List(
    Landing(YearMonth.of(2017, 7), MarDelPlata, Langostino, emptyFleet, 2),
    Landing(YearMonth.of(2017, 7), MarDelPlata, Pulpo, emptyFleet, 3),
    Landing(YearMonth.of(2017, 7), PuertoMadryn, Langostino, emptyFleet, 1),
    Landing(YearMonth.of(2017, 7), PuertoMadryn, Pulpo, emptyFleet, 1),
    Landing(YearMonth.of(2017, 8), MarDelPlata, Langostino, emptyFleet, 10),
    Landing(YearMonth.of(2017, 8), MarDelPlata, Pulpo, emptyFleet, 7),
    Landing(YearMonth.of(2017, 8), PuertoMadryn, Langostino, emptyFleet, 9),
    Landing(YearMonth.of(2017, 8), PuertoMadryn, Pulpo, emptyFleet, 4),
  )

}
