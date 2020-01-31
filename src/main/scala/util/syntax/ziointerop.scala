package util.syntax

object ziointerop {

  import zio.ZIO

  object stringops {

    import api.Main.AppTask
    import io.circe.Json
    import io.circe.literal._
    import org.http4s._
    import org.http4s.circe._
    import org.http4s.implicits._
    import zio.interop.catz._

    implicit class StringOps(self: String) {

      def runOn(httpApp: HttpApp[AppTask], uri: Uri = uri"/api/graphql", method: Method = Method.POST) =
        httpApp.run(Request(method, uri).withEntity(json"""{ "query": $self }""")) flatMap (_.as[Json])

    }
  }

  object iterableops {

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

  }

  object catsops {

    import cats.effect.{ IO => CatsIO }
    import zio.Runtime
    import zio.interop.catz.taskEffectInstance

    implicit final class ZIOOps[-R, +E <: Throwable, +A](private val self: ZIO[R, E, A]) extends AnyVal {

      def toCatsIO(implicit runtime: Runtime[R]): CatsIO[A] = taskEffectInstance toIO self

    }

  }

  object doobieops {

    import doobie.ConnectionIO
    import zio.Runtime
    import zio.interop.catz.taskEffectInstance

    implicit final class ZIOOps[-R, +E <: Throwable, A](private val self: ZIO[R, E, A]) extends AnyVal {

      def toConnectionIO(implicit runtime: Runtime[R]): ConnectionIO[A] =
        taskEffectInstance.toIO(self).to[ConnectionIO]

    }

  }

}