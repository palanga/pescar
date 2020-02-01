package api

import api.Main.AppEnv
import caliban.GraphQL.graphQL
import caliban.RootResolver
import caliban.Value.StringValue
import caliban.schema.{ GenericSchema, Schema }

object graphql extends GenericSchema[AppEnv] {

  import java.time.YearMonth

  import api.types.Metric.Landing
  import api.types._

  //  import java.time.format.DateTimeFormatter._
  implicit val yearMonthSchema: Schema.Typeclass[YearMonth] =
    scalarSchema[YearMonth](
      "YearMonth",
      None,
      yearMonth => StringValue(yearMonth.getYear.toString ++ "-" ++ yearMonth.getMonthValue.toString)
    )

  case class Queries(
    metrics: List[api.types.Metric],
  )

  private val emptyLanding =
    Landing(
      YearMonth.of(0, 1),
      Fleet(""),
      Location(Port("", None), Department(""), Province("")),
      Specie("", Category(""), CategoryGroup("")),
      0
    )

  val make =
    graphQL(
      RootResolver(
        Queries(
          emptyLanding :: Nil
        )
      )
    )

}
