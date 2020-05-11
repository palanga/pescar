package time

import java.time.format.DateTimeFormatter.ISO_LOCAL_DATE
import java.time.{ YearMonth => JYearMonth }

object syntax {

  implicit final class StringOps(private val self: String) extends AnyVal {
    def toYearMonthOption = time.YearMonth.parse(self).toOption
  }

  /**
   * Given a java YearMonth convert to a string with a format YYYY-MM
   */
  implicit final class JYearMonthOps(private val self: JYearMonth) extends AnyVal {
    def show = self.atDay(1).format(ISO_LOCAL_DATE).dropRight(3)
  }

}
