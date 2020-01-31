package api

import io.circe.Json
import io.circe.literal._
import org.http4s._
import org.http4s.implicits._
import org.http4s.circe._
import api.Main.{ httpApp, AppEnv }
import zio.ZIO
import zio.interop.catz._

object syntax {

  object gqlquery {

    implicit class StringOps(self: String) {
      def runAsJson: ZIO[AppEnv, Throwable, Json] =
        httpApp.run(
          Request(Method.POST, uri"/api/graphql").withEntity(json"""{ "query": $self }""")
        ) >>= (_.as[Json])
    }

  }

}
