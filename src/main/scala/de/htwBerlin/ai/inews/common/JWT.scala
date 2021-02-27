package de.htwBerlin.ai.inews.common

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directive1
import akka.http.scaladsl.server.Directives.{complete, optionalHeaderValueByName, provide}
import authentikat.jwt.{JsonWebToken, JwtClaimsSet, JwtClaimsSetMap, JwtHeader}
import org.json4s.DefaultFormats
import org.json4s.native.JsonMethods._

import java.util.concurrent.TimeUnit

/**
 * Object for handling JWT functionalities
 */
object JWT {
  private val tokenExpiryPeriodInDays = 1
  // TODO: use secure secret key (has to match the one on frontend!)
  private val secretKey = "secret" //SecureRandom.getInstanceStrong.toString
  private val header = JwtHeader("HS256")

  /**
   * Generates a JWT token.
   *
   * @param user       username
   * @param id         user id
   * @param rememberMe remember me
   * @param darkMode   dark mode
   * @return JsonWebToken
   */
  def generateToken(user: String, id: String, rememberMe: Boolean, darkMode: Boolean, suggestions: Boolean): String = JsonWebToken(
    header, setClaims(user, id, rememberMe, darkMode, suggestions), secretKey
  )

  /**
   * Sets JWT claims for use in token.
   *
   * @param user       username
   * @param id         user id
   * @param rememberMe remember me (token does not expire if true)
   * @param darkMode   dark mode
   * @return JwtClaimSet
   */
  private def setClaims(user: String, id: String, rememberMe: Boolean, darkMode: Boolean, suggestions: Boolean): JwtClaimsSetMap = JwtClaimsSet(
    if (rememberMe)
      Map("user" -> user, "id" -> id, "darkMode" -> darkMode, "suggestions" -> suggestions)
    else
      Map("user" -> user, "id" -> id, "darkMode" -> darkMode, "suggestions" -> suggestions,
        "expiredAt" -> (System.currentTimeMillis() + TimeUnit.DAYS.toMillis(tokenExpiryPeriodInDays)))
  )

  /**
   * Checks if JWT from authorization header is authenticated.
   *
   * @return HTTP 401 if unauthorized, else JWT claims
   */
  def authenticated: Directive1[Map[String, Any]] =
    optionalHeaderValueByName("Authorization").flatMap {
      case Some(jwt) if isTokenExpired(jwt) =>
        complete(StatusCodes.Unauthorized -> "Token expired.")

      case Some(jwt) if JsonWebToken.validate(jwt, secretKey) =>
        provide(getClaims(jwt).getOrElse(Map.empty[String, Any]))

      case None => complete(StatusCodes.Unauthorized)
    }

  /**
   * Checks if given token is expired.
   *
   * @param jwt token
   * @return true if expired
   */
  private def isTokenExpired(jwt: String) = getClaims(jwt) match {
    case Some(claims) =>
      claims.get("expiredAt") match {
        case Some(value) => value.toString.toLong < System.currentTimeMillis()
        case None => false
      }
    case None => false
  }

  /**
   * Retrieves claims from token.
   *
   * @param jwt token
   * @return map of claims
   */
  private def getClaims(jwt: String): Option[Map[String, Any]] = {
    implicit val formats: DefaultFormats.type = DefaultFormats
    val token = JsonWebToken.unapply(jwt).get
    Option(parse(token._2.asJsonString).extract[Map[String, Any]].map(x => (x._1, x._2)))
  }
}
