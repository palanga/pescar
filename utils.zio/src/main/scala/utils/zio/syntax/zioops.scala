package utils.zio.syntax

object zioops {

  import zio.ZIO
  import zio.clock.Clock
  import zio.console.{ putStrLn, Console }
  import zio.stream.ZStream

  implicit final class ZStreamOps[-R, +E, +A](private val self: ZStream[R, E, A]) extends AnyVal {

    /**
     * Uncurry in order to get better type inference
     */
    def mapMPar_[R1 <: R, E1 >: E, B](n: Int, f: A => ZIO[R1, E1, B]): ZStream[R1, E1, B] =
      self.mapMPar[R1, E1, B](n)(f)

    def tapPrint(show: A => String): ZStream[Console with R, E, A] = self.tap(putStrLn _ compose show)

  }

  implicit final class ZIOOps[-R, +E, +A](private val self: ZIO[R, E, A]) extends AnyVal {

    def tapPrint(show: A => String): ZIO[Console with R, E, A] = self.tap(putStrLn _ compose show)

    def tapPrintTimed(prefix: String = "Elapsed time: "): ZIO[Console with R with Clock, E, A] =
      self.timed.tap(timed => putStrLn(prefix ++ timed._1.render)).map(_._2)

    def times(n: Int): Iterable[ZIO[R, E, A]] = ZIO.replicate(n)(self)

  }

}
