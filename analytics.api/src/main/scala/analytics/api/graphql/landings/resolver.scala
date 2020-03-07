package analytics.api.graphql.landings

import java.time.YearMonth

import analytics.api.database.landings.Dummy._
import analytics.api.database.landings.landings_tables.{ FLEETS_ALL, LOCATIONS_ALL, SPECIES_ALL }
import analytics.api.graphql.landings.types.{ Args, Node }
import analytics.api.types.Metric.LandingsSummary
import analytics.api.types.{ Filter, FleetName, LocationName, SpecieName }
import utils.syntax.list.PairListOps
import zio.UIO

object resolver {

  def locations = UIO succeed LOCATIONS_ALL.toList.map(_.name).sorted
  def species   = UIO succeed SPECIES_ALL.toList.map(_.name).sorted
  def fleets    = UIO succeed FLEETS_ALL.toList.map(_.name).sorted

  def fromArgs(args: Args) =
    fromFilter(
      Filter(
        args.dateRange.toSet,
        args.locations.map(_.toSet) getOrElse LOCATIONS_ALL.map(_.name),
        args.species.map(_.toSet) getOrElse SPECIES_ALL.map(_.name),
        args.fleets.map(_.toSet) getOrElse FLEETS_ALL.map(_.name),
      )
    )

  private def fromFilter(filter: Filter): Node =
    Node(
      landings = query.landingsFromFilter(filter),
      summary = query.landingsSummaryFromFilter(filter).map(LandingsSummary),
      byDate = UIO effectTotal byKey(filter, _.dates, _ withDates _),
      byLocation = UIO effectTotal byKey(filter, _.locations, _ withLocations _),
      bySpecie = UIO effectTotal byKey(filter, _.species, _ withSpecies _),
      byFleet = UIO effectTotal byKey(filter, _.fleets, _ withFleets _),
    )

  private def byKey[K](filter: Filter, getKey: Filter => Set[K], updateFilter: (Filter, Set[K]) => Filter)(
    implicit ord: Ordering[K]
  ) =
    getKey(filter).toList match {
      case Nil  => Map.empty[K, Node]
      case keys => keys.sorted.map(key => key -> fromFilter(updateFilter(filter, Set(key)))).toSortedMap
    }

  private implicit class FilterOps(val filter: Filter) extends AnyVal {
    def withDates(dates: Set[YearMonth]): Filter            = filter.copy(dates = dates)
    def withLocations(locations: Set[LocationName]): Filter = filter.copy(locations = locations)
    def withSpecies(species: Set[SpecieName]): Filter       = filter.copy(species = species)
    def withFleets(fleets: Set[FleetName]): Filter          = filter.copy(fleets = fleets)
  }

}
