package api

import java.time.YearMonth

import api.Main.AppEnv
import caliban.GraphQL.graphQL
import caliban.RootResolver
import caliban.Value.StringValue
import caliban.schema.{ GenericSchema, Schema }
import util.GeoDegree.{ Latitude, Longitude }

object graphql extends GenericSchema[AppEnv] {

  import java.time.YearMonth
  import java.time.format.DateTimeFormatter.ISO_LOCAL_DATE

  implicit val yearMonthSchema: Schema.Typeclass[YearMonth] =
    scalarSchema[YearMonth](
      "YearMonth",
      None,
      yearMonth => StringValue(yearMonth.atDay(1).format(ISO_LOCAL_DATE).dropRight(3)) // yyyy-mm-dd -> yyyy-mm
    )

  implicit val latitudeSchema: Schema.Typeclass[Latitude]   = Schema.floatSchema.contramap(_.value)
  implicit val longitudeSchema: Schema.Typeclass[Longitude] = Schema.floatSchema.contramap(_.value)

  case class Queries(
    landings: resolvers.LandingsSummaryResolver,
  )

  val make =
    graphQL(
      RootResolver(
        Queries(
          resolvers.LandingsSummaryResolver(),
        )
      )
    )

}

object resolvers {

  import types.Metric.LandingsSummary

  // TODO
  case class LandingsSummaryResolver(byDate: Map[YearMonth, LandingsSummary] = Map.empty)

}
