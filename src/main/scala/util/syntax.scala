package util

import cats.effect.{ IO => CatsIO }
import doobie.ConnectionIO
import zio.clock.Clock
import zio.console.{ putStrLn, Console }
import zio.interop.catz.taskEffectInstance
import zio.stream.ZStream
import zio.{ Runtime, ZIO }

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

    implicit final class ZIOOps[-R, +E, +A](private val self: ZIO[R, E, A]) extends AnyVal {

      def tapPrint(show: A => String): ZIO[Console with R, E, A] = self.tap(putStrLn _ compose show)

      def tapPrintTimed(prefix: String = "Elapsed time: "): ZIO[Console with R with Clock, E, A] =
        self.timed.tap(timed => putStrLn(prefix ++ timed._1.render)).map(_._2)

      def times(n: Int): Iterable[ZIO[R, E, A]] = ZIO.replicate(n)(self)

    }

    implicit final class IterableOps[-R, +E, +A](private val self: Iterable[ZIO[R, E, A]]) extends AnyVal {

      /**
       * Evaluate each effect in the structure from left to right, and collect
       * the results. For a parallel version, see `collectAllPar`.
       */
      def collectAll: ZIO[R, E, Iterable[A]] = ZIO collectAll self

      /**
       * Evaluate each effect in the structure in parallel, and collect
       * the results. For a sequential version, see `collectAll`.
       */
      def collectAllPar: ZIO[R, E, Iterable[A]] = ZIO collectAllPar self

      /**
       * Evaluate each effect in the structure in parallel, and collect
       * the results. For a sequential version, see `collectAll`.
       *
       * Unlike `collectAllPar`, this method will use at most `n` fibers.
       */
      def collectAllParN(n: Int): ZIO[R, E, Iterable[A]] = ZIO.collectAllParN(n)(self)

    }

    object interop {

      object cats {

        implicit final class ZIOOps[-R, +E <: Throwable, +A](private val self: ZIO[R, E, A]) extends AnyVal {

          def toCatsIO(implicit runtime: Runtime[R]): CatsIO[A] = taskEffectInstance toIO self

        }

      }

      object doobie {

        implicit final class ZIOOps[-R, +E <: Throwable, A](private val self: ZIO[R, E, A]) extends AnyVal {

          def toConnectionIO(implicit runtime: Runtime[R]): ConnectionIO[A] =
            taskEffectInstance.toIO(self).to[ConnectionIO]

        }

      }

    }

  }

}
