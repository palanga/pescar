package util

import zio.ZIO
import zio.stream.ZStream

object syntax {

  object either {

    implicit final class EitherOps[+E, +A](private val self: Either[E, A]) extends AnyVal {

      def bimap[E1, A1](f: E => E1, g: A => A1): Either[E1, A1] = self.left.map(f).map(g)

    }

  }

  object zio {

    implicit final class ZStreamOps[-R, +E, +A](private val self: ZStream[R, E, A]) extends AnyVal {

      /**
       * Uncurry in order to get better type inference
       */
      def mapMPar_[R1 <: R, E1 >: E, B](n: Int, f: A => ZIO[R1, E1, B]): ZStream[R1, E1, B] =
        self.mapMPar[R1, E1, B](n)(f)

    }

  }

}
