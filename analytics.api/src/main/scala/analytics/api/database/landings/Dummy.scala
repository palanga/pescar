package analytics.api.database.landings

import java.time.YearMonth

import analytics.api.database.landings.landings_summary_tables._
import analytics.api.database.landings.landings_tables._
import analytics.api.types.Location.{ Harbour, Miscellaneous }
import analytics.api.types.Metric.Landing
import analytics.api.types.{ Filter, Fleet, Location, Specie }
import utils.GeoLocation
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

  val memo: scala.collection.mutable.Map[YearMonth, Landing] = scala.collection.mutable.Map.empty

  def landingsByDate(dates: Set[YearMonth]) = {

    import analytics.api.types.Metric.Landing
    import analytics.consumer.gob.datos.database.landing.{ DoobieLandingsDatabase, LandingsDatabase }
    import analytics.consumer.gob.datos.types.{ Landing => ConsumerLanding }

    val managed = DoobieLandingsDatabase.makeManagedWithBlocking

    def toApiLanding(landing: ConsumerLanding): Landing = landing match {
      case ConsumerLanding(fecha, flota, puerto, _, _, _, _, lat, lon, _, especie, _, captura) =>
        val location = (lat zip lon).fold[Location](Miscellaneous(puerto))(
          geoLocation => Harbour(puerto, GeoLocation.fromFloatPairUnsafe(geoLocation))
        )
        Landing(fecha, location, Specie(especie), Fleet(flota), captura)
    }

    def fromDB(dates: Set[YearMonth]) =
      LandingsDatabase.module
        .find(dates)
        .provideSomeManaged(managed)
        .map(toApiLanding)
        .catchAll(throw _)
        .provide(zio.blocking.Blocking.Live)

    //    val zioRes = fromDB(dates.filterNot(memo contains _))
    //      .runCollect
    //      .map(landings => memo ++= landings.map(landing => (landing.date, landing)))
    //      .as(dates.map(date => memo(date)))
    //
    //    ZStream.fromEffect(zioRes).flatMap(ZStream.fromIterable)

    fromDB(dates)

  }

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

  val LOCATIONS_ALL = Set(
    Location.Harbour("Almanza", GeoLocation.zero),
    Location.Harbour("Bahía Blanca", GeoLocation.zero),
    Location.Harbour("Caleta Cordova", GeoLocation.zero),
    Location.Harbour("Caleta Olivia / Paula", GeoLocation.zero),
    Location.Harbour("Camarones", GeoLocation.zero),
    Location.Harbour("Comodoro Rivadavia", GeoLocation.zero),
    Location.Harbour("General Lavalle", GeoLocation.zero),
    Location.Harbour("Mar del Plata", GeoLocation.zero),
    Location.Harbour("Necochea / Quequén", GeoLocation.zero),
    Location.Harbour("Puerto Deseado", GeoLocation.zero),
    Location.Harbour("Puerto Madryn", GeoLocation.zero),
    Location.Harbour("Punta Colorada", GeoLocation.zero),
    Location.Harbour("Punta Quilla", GeoLocation.zero),
    Location.Harbour("Rawson", GeoLocation.zero),
    Location.Harbour("Rosales", GeoLocation.zero),
    Location.Harbour("Río Salado", GeoLocation.zero),
    Location.Harbour("San Antonio Este", GeoLocation.zero),
    Location.Harbour("San Antonio Oeste", GeoLocation.zero),
    Location.Harbour("San Clemente del Tuyú", GeoLocation.zero),
    Location.Harbour("San Julián", GeoLocation.zero),
    Location.Harbour("Ushuaia", GeoLocation.zero),
    Location.Miscellaneous("otros puertos"),
    Location.Miscellaneous("otros puertos Buenos Aires"),
  )

  val SPECIES_ALL = Set(
    Specie("Abadejo"),
    Specie("Almejas nep"),
    Specie("Anchoa de banco"),
    Specie("Anchoíta"),
    Specie("Bacalao austral"),
    Specie("Bagre"),
    Specie("Besugo"),
    Specie("Bonito"),
    Specie("Brótola"),
    Specie("Caballa"),
    Specie("Cabrilla"),
    Specie("Calamar Illex"),
    Specie("Calamar Loligo"),
    Specie("Calamar patagónico"),
    Specie("Camarón"),
    Specie("Cangrejo"),
    Specie("Caracol"),
    Specie("Castañeta"),
    Specie("Cazón"),
    Specie("Centolla"),
    Specie("Centollón"),
    Specie("Chernia"),
    Specie("Cholga"),
    Specie("Chucho"),
    Specie("Congrio de profundidad"),
    Specie("Congrio"),
    Specie("Cornalito"),
    Specie("Corvina blanca"),
    Specie("Corvina negra"),
    Specie("Gatuzo"),
    Specie("Granadero"),
    Specie("Jurel"),
    Specie("Langostino"),
    Specie("Lenguados nep"),
    Specie("Lisa"),
    Specie("Lurión común"),
    Specie("Mejillón"),
    Specie("Merluza austral"),
    Specie("Merluza de cola"),
    Specie("Merluza hubbsi"),
    Specie("Merluza negra"),
    Specie("Mero"),
    Specie("Notothenia"),
    Specie("Otras - Algas, etc."),
    Specie("Otras especies de crustác"),
    Specie("Otras especies de molusco"),
    Specie("Otras especies de peces"),
    Specie("Palometa"),
    Specie("Pampanito"),
    Specie("Papafigo"),
    Specie("Pargo"),
    Specie("Pejerrey"),
    Specie("Pescadilla real"),
    Specie("Pescadilla"),
    Specie("Pez gallo"),
    Specie("Pez limón"),
    Specie("Pez palo"),
    Specie("Pez sable"),
    Specie("Pez ángel"),
    Specie("Polaca"),
    Specie("Pulpitos"),
    Specie("Pulpos nep"),
    Specie("Raya cola corta"),
    Specie("Raya de círculos"),
    Specie("Raya espinosa"),
    Specie("Raya hocicuda / picuda"),
    Specie("Raya lisa"),
    Specie("Raya marmolada"),
    Specie("Raya marrón oscuro"),
    Specie("Raya pintada"),
    Specie("Rayas nep"),
    Specie("Rubio"),
    Specie("Róbalo"),
    Specie("Salmonete"),
    Specie("Salmón de mar"),
    Specie("Saraca"),
    Specie("Sargo"),
    Specie("Savorín"),
    Specie("Tiburones nep"),
    Specie("Tiburón azul"),
    Specie("Tiburón bacota"),
    Specie("Tiburón escalandrún"),
    Specie("Tiburón espinoso"),
    Specie("Tiburón gris"),
    Specie("Tiburón martillo"),
    Specie("Tiburón moteado"),
    Specie("Tiburón peregrino"),
    Specie("Tiburón pintaroja"),
    Specie("Variado costero"),
    Specie("Vieira (callos)"),
  )

  val FLEETS_ALL = Set(
    Fleet("Congeladores arrastreros"),
    Fleet("Congeladores palangreros"),
    Fleet("Congeladores poteros nacionales"),
    Fleet("Congeladores tangoneros"),
    Fleet("Congeladores trampas"),
    Fleet("Costeros"),
    Fleet("Fresqueros"),
    Fleet("Rada o ría"),
  )

  private val MarDelPlata  = Location.Harbour("Mar del Plata", GeoLocation.zero)
  private val PuertoMadryn = Location.Harbour("Puerto Madryn", GeoLocation.zero)

  private val Langostino    = Specie("Langostino")
  private val MerluzaHubbsi = Specie("Merluza hubbsi")

  private val RadaORia   = Fleet("Rada o ría")
  private val Fresqueros = Fleet("Fresqueros")

  private val all = List(
    Landing(YearMonth.of(2017, 7), MarDelPlata, Langostino, RadaORia, 2),
    Landing(YearMonth.of(2017, 7), MarDelPlata, MerluzaHubbsi, RadaORia, 3),
    Landing(YearMonth.of(2017, 7), PuertoMadryn, Langostino, RadaORia, 1),
    Landing(YearMonth.of(2017, 7), PuertoMadryn, MerluzaHubbsi, Fresqueros, 1),
    Landing(YearMonth.of(2017, 8), MarDelPlata, Langostino, RadaORia, 10),
    Landing(YearMonth.of(2017, 8), MarDelPlata, MerluzaHubbsi, RadaORia, 7),
    Landing(YearMonth.of(2017, 8), PuertoMadryn, Langostino, RadaORia, 9),
    Landing(YearMonth.of(2017, 8), PuertoMadryn, MerluzaHubbsi, Fresqueros, 4),
  )

}
