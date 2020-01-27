package time

object syntax {

  implicit final class StringOps(private val self: String) extends AnyVal {
    def toYearMonthOption = time.YearMonth.parse(self).toOption
  }

}
