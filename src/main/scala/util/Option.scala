package util

object Option {
  def fromNullabe[A](nullable: A) = scala.Option.when(nullable != null)(nullable)
}
