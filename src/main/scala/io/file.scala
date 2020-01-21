package io

import zio.ZIO
import zio.nio.channels.AsynchronousFileChannel
import zio.nio.file.Path
import zio.stream.{ Sink, Stream }

object file {

  /**
   * Open a whole file as a String
   */
  def open(path: String) =
    openAsyncChannel(path).use(asString)

  /**
   * Open a whole resource file as a String
   */
  def openResource(resourcePath: String) =
    ZIO
      .fromOption(util.Option.fromNullabe(this.getClass.getResource(resourcePath)))
      .map(_.getFile)
      .asError(new java.io.IOException(s"Resource not found: $resourcePath"))
      .flatMap(open)

  /**
   * List file lines
   */
  def list(path: String) =
    openAsyncChannel(path).use(asStringList)

  /**
   * Stream file lines
   */
  def stream(path: String) =
    Stream.unwrapManaged(openAsyncChannel(path).map(asStringStream))

  private def openAsyncChannel(uri: String) =
    for {
      path    <- ZIO.effect(Path(uri)).toManaged_
      channel <- AsynchronousFileChannel.open(path)
    } yield channel

  private def asString(channel: AsynchronousFileChannel) =
    for {
      size <- channel.size
      wholeFile <- if (size > Int.MaxValue.toLong)
                    ZIO.fail(new java.io.IOException(s"File too large: $size bytes"))
                  else channel.read(size.toInt, 0L)
      decoded <- Stream(wholeFile) // Create a stream just to use Sink.utf8DecodeChunk and Sink.splitLines
                  .aggregate(Sink.utf8DecodeChunk)
                  .runHead
                  .map(_.getOrElse(""))
    } yield decoded

  private def asStringList(channel: AsynchronousFileChannel) =
    for {
      size <- channel.size
      wholeFile <- if (size > Int.MaxValue.toLong)
                    ZIO.fail(new java.io.IOException(s"File too large: $size bytes"))
                  else channel.read(size.toInt, 0L)
      decoded <- Stream(wholeFile) // Create a stream just to use Sink.utf8DecodeChunk and Sink.splitLines
                  .aggregate(Sink.utf8DecodeChunk)
                  .aggregate(Sink.splitLines)
                  .flatMap(Stream.fromChunk)
                  .runCollect
    } yield decoded

  private val OneMegaByte  = 1024 * 1024
  private val OneMegaByteL = OneMegaByte.toLong
  private def asStringStream(channel: AsynchronousFileChannel) =
    Stream
      .iterate(0L)(_ + OneMegaByteL) // TODO paginate ?
      .mapM(channel.read(OneMegaByte, _))
      .takeUntil(_.isEmpty)
      .aggregate(Sink.utf8DecodeChunk)
      .aggregate(Sink.splitLines)
      .flatMap(Stream.fromChunk)

}
