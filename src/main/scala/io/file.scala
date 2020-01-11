package io

import zio.ZIO
import zio.nio.channels.AsynchronousFileChannel
import zio.nio.file.Path
import zio.stream.{ Sink, Stream }

object file {

  /**
   * Open a whole file in a ZIO as a List of its lines
   */
  def open(path: String) =
    AsynchronousFileChannel.open(Path(path)).use(toStringList)

  /**
   * Stream the lines of a file
   */
  def stream(path: String) =
    Stream.unwrapManaged(AsynchronousFileChannel.open(Path(path)).map(toStringStream))

  private def toStringList(channel: AsynchronousFileChannel) =
    for {
      size <- channel.size
      wholeFile <- if (size > Int.MaxValue.toLong)
                    ZIO.fail(new java.io.IOException(s"File too large: $size bytes")) // TODO meh
                  else channel.read(size.toInt, 0L)
      decoded <- Stream(wholeFile) // Create a stream just to use Sink.utf8DecodeChunk and Sink.splitLines
                  .aggregate(Sink.utf8DecodeChunk)
                  .aggregate(Sink.splitLines)
                  .flatMap(Stream.fromChunk)
                  .runCollect
    } yield decoded

  private val OneMegaByte  = 1024 * 1024
  private val OneMegaByteL = OneMegaByte.toLong
  private def toStringStream(channel: AsynchronousFileChannel) =
    Stream
      .iterate(0L)(_ + OneMegaByteL)
      .mapM(channel.read(OneMegaByte, _))
      .takeUntil(_.isEmpty)
      .aggregate(Sink.utf8DecodeChunk)
      .aggregate(Sink.splitLines)
      .flatMap(Stream.fromChunk)

}
