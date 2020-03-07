package analytics.api.graphql.landings

import java.time.YearMonth

import analytics.api.database.landings.Dummy
import analytics.api.types.Filter
import analytics.api.types.Metric.Landing
import zquery.{ CompletedRequestMap, DataSource, Request, ZQuery }

case class LandingsFromFilter(filter: Filter) extends Request[Nothing, List[Landing]]

object query {

  def landingsFromFilter(filter: Filter) = ZQuery.fromRequest(LandingsFromFilter(filter))(LandingsDataSource)

  def landingsSummaryFromFilter(filter: Filter) = landingsFromFilter(filter).map(_.map(_.catchCount).sum)

  private val LandingsDataSource: DataSource[Any, LandingsFromFilter] =
    DataSource("LandingDataSource") { requests =>
      Dummy
        .landingsFromFilter(requests.map(_.filter).reduce(_ union _))
        .runCollect
        .map(makeResultMap(requests))
    }

  /**
   * O(n * log n)
   */
  private def makeResultMap(requests: Iterable[LandingsFromFilter])(landings: List[Landing]) = {

    import utils.syntax.list.QuintetListOps

    /**
     * Group once so we can find in O(log n) later
     */
    val grouped: Map[YearMonth, Map[String, Map[String, Map[String, Landing]]]] =
      landings
        .map(landing => (landing.date, landing.location.name, landing.specie.name, landing.fleet.name, landing))
        .toTetraMap

    def findResult(filter: Filter) =
      for {
        date     <- filter.dates
        location <- filter.locations
        specie   <- filter.species
        fleet    <- filter.fleets
      } yield
        grouped
          .get(date)
          .flatMap(_.get(location))
          .flatMap(_.get(specie))
          .flatMap(_.get(fleet))

    // TODO move and make better
    def landingComparator(a: Landing, b: Landing): Boolean = {

      def compareNoDateNoLocationNoSpecie: Boolean =
        if (a.fleet.name.compareTo(b.fleet.name) > 0) false
        else if (a.fleet.name.compareTo(b.fleet.name) < 0) true
        else true

      def compareNoDateNoLocation: Boolean =
        if (a.specie.name.compareTo(b.specie.name) > 0) false
        else if (a.specie.name.compareTo(b.specie.name) < 0) true
        else compareNoDateNoLocationNoSpecie

      def compareNoDate: Boolean =
        if (a.location.name.compareTo(b.location.name) > 0) false
        else if (a.location.name.compareTo(b.location.name) < 0) true
        else compareNoDateNoLocation

      if (a.date isAfter b.date) false
      else if (a.date isBefore b.date) true
      else compareNoDate

    }

    requests.foldLeft(CompletedRequestMap.empty)(
      (resultMap, req) => {
        val result = findResult(req.filter) collect { case Some(landing) => landing }
        resultMap.insert(req)(Right(result.toList sortWith landingComparator))
      }
    )

  }

  // TODO move
  private implicit class FilterOps(val self: Filter) extends AnyVal {
    def union(other: Filter): Filter =
      Filter(
        self.dates ++ other.dates,
        self.locations ++ other.locations,
        self.species ++ other.species,
        self.fleets ++ other.fleets
      )
  }

}
