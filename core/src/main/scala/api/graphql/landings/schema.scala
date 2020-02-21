package api.graphql.landings

import java.time.YearMonth
import java.time.format.DateTimeFormatter.ISO_LOCAL_DATE

import api.Main.AppEnv
import api.graphql.landings.types.Queries
import api.types.Metric.LandingsSummary
import api.types.{ Fleet, Location, Specie }
import caliban.GraphQL.graphQL
import caliban.schema.{ ArgBuilder, GenericSchema, Schema }
import caliban.{ CalibanError, RootResolver }
import utils.GeoDegree.{ Latitude, Longitude }

object schema extends GenericSchema[AppEnv] {

  private implicit val yearMonthSchema: Schema.Typeclass[YearMonth] =
    Schema.stringSchema.contramap(_.atDay(1).format(ISO_LOCAL_DATE).dropRight(3)) // yyyy-mm-dd -> yyyy-mm

  private implicit val yearMonthArgBuilder: ArgBuilder[YearMonth] =
    ArgBuilder.string
      .flatMap(time.YearMonth.parse _ andThen (_.toEither.left.map(calibanExecutionErrorFromThrowable)))

  private def calibanExecutionErrorFromThrowable(t: Throwable) =
    CalibanError.ExecutionError(msg = t.getMessage, innerThrowable = Some(t))

  private implicit val latitudeSchema : Schema.Typeclass[Latitude]  = Schema.floatSchema.contramap(_.value)
  private implicit val longitudeSchema: Schema.Typeclass[Longitude] = Schema.floatSchema.contramap(_.value)

  private implicit val landingsSummarySchema: Schema.Typeclass[LandingsSummary] = Schema.intSchema.contramap(_.total)

  private implicit val locationSchema: Schema.Typeclass[Location] = Schema.stringSchema.contramap(_.name)
  private implicit val SpecieSchema  : Schema.Typeclass[Specie]   = Schema.stringSchema.contramap(_.name)
  private implicit val fleetSchema   : Schema.Typeclass[Fleet]    = Schema.stringSchema.contramap(_.name)

  val make =
    graphQL(
      RootResolver(
        Queries(
          resolver.fromArgs,
        )
      )
    )

}
