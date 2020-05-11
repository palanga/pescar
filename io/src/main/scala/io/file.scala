package io

import java.io.IOException
import java.nio.file.{ OpenOption, StandardOpenOption }

import zio.nio.channels.AsynchronousFileChannel
import zio.nio.core.file.Path
import zio.stream.{ Sink, Stream }
import zio.{ Chunk, ZIO }

object file {

  /**
   * Open a whole file as a String
   */
  def open(path: String) = openAsyncChannel(path).use(decodeAsString)

  /**
   * List file lines
   */
  def list(path: String) = openAsyncChannel(path).use(decodeAsStringList)

  /**
   * Stream file lines
   */
  def stream(path: String) = Stream.unwrapManaged(openAsyncChannel(path).map(decodeAsStringStream))

  def write(path: String, content: String) =
    openAsyncChannel(path, StandardOpenOption.WRITE)
      .use(_.write(Chunk.fromArray(content.toArray.map(_.toByte)), 0L))

  private def openAsyncChannel(uri: String, options: OpenOption*) =
    for {
      path    <- ZIO.effect(Path(uri)).toManaged_
      channel <- AsynchronousFileChannel.open(path, options: _*)
    } yield channel

  private def decodeAsString(channel: AsynchronousFileChannel) =
    for {
      size      <- channel.size
      wholeFile <- if (size > Int.MaxValue.toLong)
                     ZIO.fail(new IOException(s"File too large: $size bytes"))
                   else channel.read(size.toInt, 0L)
      decoded   <- Stream(wholeFile) // Create a stream just to use Sink.utf8DecodeChunk and Sink.splitLines
                   .aggregate(Sink.utf8DecodeChunk)
                   .runHead
                   .map(_.getOrElse(""))
    } yield decoded

  private def decodeAsStringList(channel: AsynchronousFileChannel) =
    for {
      size      <- channel.size
      wholeFile <- if (size > Int.MaxValue.toLong)
                     ZIO.fail(new IOException(s"File too large: $size bytes"))
                   else channel.read(size.toInt, 0L)
      decoded   <- Stream(wholeFile) // Create a stream just to use Sink.utf8DecodeChunk and Sink.splitLines
                   .aggregate(Sink.utf8DecodeChunk)
                   .aggregate(Sink.splitLines)
                   .flatMap(Stream fromChunk _)
                   .runCollect
    } yield decoded

  private val OneMegaByte                                            = 1024 * 1024
  private val OneMegaByteL                                           = OneMegaByte.toLong
  private def decodeAsStringStream(channel: AsynchronousFileChannel) =
    Stream
      .iterate(0L)(_ + OneMegaByteL)
      .mapM(channel.read(OneMegaByte, _))
      .takeUntil(_.isEmpty)
      .aggregate(Sink.utf8DecodeChunk)
      .aggregate(Sink.splitLines)
      .flatMap(Stream fromChunk _)

}
