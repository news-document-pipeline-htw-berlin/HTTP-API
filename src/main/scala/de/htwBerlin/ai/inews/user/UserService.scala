package de.htwBerlin.ai.inews.user

import java.util.concurrent.TimeUnit

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.model.headers.HttpCookie
import akka.http.scaladsl.server.{Directives, Route}
import authentikat.jwt.{JsonWebToken, JwtClaimsSet, JwtHeader}
import spray.json.{DefaultJsonProtocol, RootJsonFormat}

import scala.concurrent.ExecutionContext


// TODO: connect to DB

final case class LoginRequest(user: String, password: String)

trait JsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val loginRequestFormat: RootJsonFormat[LoginRequest] = jsonFormat2(LoginRequest)
}

class UserService()(implicit executionContext: ExecutionContext) extends Directives with JsonSupport {
  private val tokenExpiryPeriodInDays = 1
  private val secretKey = "super_secret_key"
  private val header = JwtHeader("HS256")

  private def setClaims(user: String, expiryPeriodInDays: Long) = JwtClaimsSet(
    Map("user" -> user,
      "expiredAt" -> (System.currentTimeMillis() + TimeUnit.DAYS
        .toMillis(expiryPeriodInDays)))
  )

  def handleLogin(): Route = {
    entity(as[LoginRequest]) {
      case lr@LoginRequest("admin", "admin") =>
        val claims = setClaims(lr.user, tokenExpiryPeriodInDays)
        setCookie(HttpCookie("accessToken", value = JsonWebToken(header, claims, secretKey), path = Option("/"))) {
          complete(StatusCodes.OK)
        }
      case LoginRequest(_, _) => complete(StatusCodes.Unauthorized)
    }
  }
}
