package util

/**
 * Just playing around with typeclasse
 */
object printer {

  trait Show[A] {
    def show(a: A): String
  }

  object Show {

    def apply[A](implicit sh: Show[A]): Show[A] = sh

    //needed only if we want to support notation: show(...)
    def show[A: Show](a: A) = Show[A].show(a)

    implicit class ShowOps[A: Show](a: A) {
      def show = Show[A].show(a)
    }

    implicit def eitherShow[E, A](implicit ls: Show[E], rs: Show[A]): Show[Either[E, A]] = {
      case Left(value)  => s"Left:  ${value.show}"
      case Right(value) => s"Right: ${value.show}"
    }

    implicit def lineParseErrorShow: Show[gob.datos.consumer.csv.parser.LineParseError] = _.message

    implicit def indexedError: Show[gob.datos.consumer.Main.Indexed[gob.datos.consumer.csv.parser.LineParseError]] = {
      case gob.datos.consumer.Main.Indexed(index, value) => s"Parse failure on line $index: ${value.show}"
    }

    implicit val landingShow: Show[gob.datos.consumer.types.Landing] = _.toString

  }

  def print[A](implicit sh: Show[A]) = zio.console.putStrLn _ compose Show[A].show

  def showAny(any: Any): String = any.toString

  def printMany[A](prefix: String, f: Iterable[A] => String)(seq: Iterable[A]) =
    zio.console.putStrLn(prefix ++ f(seq))

  def showMany[A](prefix: String, f: Iterable[A] => String)(seq: Iterable[A]) = prefix ++ f(seq)

}
