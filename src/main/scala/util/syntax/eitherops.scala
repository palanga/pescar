package util.syntax

object eitherops {

  implicit final class EitherOps[+E, +A](private val self: Either[E, A]) extends AnyVal {

    def bimap[E1, A1](f: E => E1, g: A => A1): Either[E1, A1] = self.left.map(f).map(g)

  }

}
