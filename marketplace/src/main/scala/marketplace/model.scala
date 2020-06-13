package marketplace

import java.time.OffsetDateTime

object model {

  case class Post(
    id: PostId,
    product: Product,
    body: Body,
    price: Price,
    location: Location,
    owner: User,
    postType: PostType = PostType.Sale,
    condition: ProductCondition = ProductCondition.New,
    status: Status = Status.Opened,
    createdAt: OffsetDateTime = OffsetDateTime.now(),
  )

  case class Product(id: ProductId, name: ProductName, tags: List[Tag])

  sealed trait PostType extends Product with Serializable
  object PostType {
    case object Sale  extends PostType
    case object Lease extends PostType
  }

  sealed trait ProductCondition extends Product with Serializable
  object ProductCondition {
    case object New  extends ProductCondition
    case object Mint extends ProductCondition
    case object Good extends ProductCondition
    case object Bad  extends ProductCondition
  }

  sealed trait Status extends Product with Serializable
  object Status {
    case object Draft  extends Status
    case object Opened extends Status
    case object Paused extends Status
    case object Closed extends Status
  }

  case class User(id: UserId, posts: List[Post] = Nil)

  class PostId(val self: String)      extends AnyVal
  class ProductId(val self: String)   extends AnyVal
  class UserId(val self: String)      extends AnyVal
  class Body(val self: String)        extends AnyVal
  class ProductName(val self: String) extends AnyVal
  class Tag(val self: String)         extends AnyVal
  class Price(val self: String)       extends AnyVal
  class Location(val self: String)    extends AnyVal

}
