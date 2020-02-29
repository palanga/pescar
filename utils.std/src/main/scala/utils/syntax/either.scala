package utils.syntax

object either {

  implicit final class EitherOps[+E, +A](private val self: Either[E, A]) extends AnyVal {

    def bimap[E1, A1](f: E => E1, g: A => A1): Either[E1, A1] = self.left.map(f).map(g)

    def zip[E1 >: E, A1](other: Either[E1, A1]): Either[List[E1], (A, A1)] =
      (self, other) match {
        case (Right(a), Right(a1)) => Right((a, a1))
        case (Left(e), Left(e1))   => Left(e :: e1 :: Nil)
        case (Left(e), _)          => Left(e :: Nil)
        case (_, Left(e1))         => Left(e1 :: Nil)
      }

    def zipWithError[E1 >: E, E2 >: E1, A1](other: Either[E1, A1])(f: (E, E1) => E2): Either[E2, (A, A1)] =
      (self, other) match {
        case (Right(a), Right(a1)) => Right((a, a1))
        case (Left(e), Left(e1))   => Left(f(e, e1))
        case (Left(e), _)          => Left(e)
        case (_, Left(e1))         => Left(e1)
      }

  }

}
