package de.htwBerlin.ai.inews.user

import java.util.concurrent.TimeUnit

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.model.headers.HttpCookie
import akka.http.scaladsl.server.{Directives, Route}
import authentikat.jwt.{JsonWebToken, JwtClaimsSet, JwtHeader}
import spray.json.{DefaultJsonProtocol, RootJsonFormat}

import scala.concurrent.ExecutionContext


final case class LoginRequest(user: String, password: String, rememberMe: Boolean)
final case class SignUpRequest(username: String, email: String, password: String, password_rep: String)
final case class UserData(id: Long, username: String, email: String, password: String,
                          suggestions: Boolean, darkMode: Boolean)

trait JsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val loginRequestFormat: RootJsonFormat[LoginRequest] = jsonFormat3(LoginRequest)
  implicit val signUpRequestFormat: RootJsonFormat[SignUpRequest] = jsonFormat4(SignUpRequest)
  implicit val userDataFormat: RootJsonFormat[UserData] = jsonFormat6(UserData)
}

object Error extends Enumeration {
  type Error = Value
  val OK, SERVER_ERROR, USER_NOT_FOUND, INVALID_PASSWORD,
  PASSWORD_MISMATCH, USERNAME_TAKEN, EMAIL_TAKEN = Value
}

class UserService()(implicit executionContext: ExecutionContext) extends Directives with JsonSupport {
  private val tokenExpiryPeriodInDays = 1
  private val secretKey = "secret"//SecureRandom.getInstanceStrong.toString
  private val header = JwtHeader("HS256")

  private def setClaims(user: String, id: Long, rememberMe: Boolean, darkMode: Boolean) = JwtClaimsSet(
    if (rememberMe)
      Map("user" -> user, "id" -> id, "darkMode" -> darkMode)
    else
      Map("user" -> user, "id" -> id, "darkMode" -> darkMode,
        "expiredAt" -> (System.currentTimeMillis() + TimeUnit.DAYS
          .toMillis(tokenExpiryPeriodInDays)))
  )

  private def checkCredentials(lr: LoginRequest): Error.Value = {
    // TODO: get userdata from DB
    // val ud = dbConnector.getUserData(lr.user)
    // TODO: remove dummy data
    val ud = UserData(1, "admin", "admin@mail.com", "admin",
      suggestions = true, darkMode = false)

    if (ud == null)
      return Error.USER_NOT_FOUND
    if (lr.password != ud.password)
      return Error.INVALID_PASSWORD

    Error.OK
  }

  private def validateSignUp(sur: SignUpRequest): Error.Value = {
    // TODO: check if username / email already exist in DB
    // if (dbConnector.existsUsername(sur.username)) return Error.USERNAME_TAKEN
    // if (dbConnector.existsEmail(sur.email)) return Error.EMAIL_TAKEN

    if (sur.password != sur.password_rep)
      return Error.PASSWORD_MISMATCH

    Error.OK
  }

  def handleLogin(lr: LoginRequest): Route = {
    checkCredentials(lr) match {
      case Error.OK => {
        // TODO: get userdata from DB
        // var userData = dbConnector.getUserData(lr.user)
        // TODO: remove dummy data
        val ud = UserData(1, "admin", "admin@mail.com", "admin",
          suggestions = true, darkMode = false)

        val claims = setClaims(ud.username, ud.id, lr.rememberMe, ud.darkMode)
        val jwtToken = JsonWebToken(header, claims, secretKey)

        setCookie(HttpCookie("accessToken", value = jwtToken, path = Option("/"))) {
          complete(StatusCodes.OK -> "Logged in successfully.")
        }
      }
      case Error.USER_NOT_FOUND => complete(StatusCodes.BadRequest -> "User " + lr.user + " does not exist.")
      case Error.INVALID_PASSWORD => complete(StatusCodes.BadRequest -> "Invalid password.")
      case Error.SERVER_ERROR => complete(StatusCodes.InternalServerError -> "Internal Server Error.")
    }
  }

  // TODO: fetch user data from database, save new user, error handling
  def handleSignUp(sur: SignUpRequest): Route = {
    validateSignUp(sur) match {
      case Error.OK => {
        // TODO: create user entry in DB
        // val ud = dbConnector.createUser(sur.username, sur.email, sur.password)
        // TODO: remove dummy data
        val ud = UserData(1, sur.username, sur.email, sur.password,
          suggestions = true, darkMode = false)
        complete(StatusCodes.OK, "A new account for " + ud.username + " was created.")
      }
      case Error.USERNAME_TAKEN => complete(StatusCodes.BadRequest -> "Username already taken.")
      case Error.EMAIL_TAKEN => complete(StatusCodes.BadRequest -> "Email is already in use.")
      case Error.PASSWORD_MISMATCH => complete(StatusCodes.BadRequest -> "Password mismatch.")
      case Error.SERVER_ERROR => complete(StatusCodes.InternalServerError -> "Internal Server Error.")
    }
  }

  def getUserData(id: Long) : Route = {
    // TODO: get user from DB
    // val ud = dbConnector.getUserData(id)
    // TODO: remove dummy data
    val ud = UserData(1, "admin", "admin@mail.com", "password",
      suggestions = true, darkMode = false)
    if (ud == null)
      complete(StatusCodes.NotFound, "User with id " + id + " does not exist.")
    complete(StatusCodes.OK, ud)
  }

  def updateUserData(ud: UserData) : Route = {
    // TODO: update user in DB (make sure username CANNOT be changed!)
    // if (!dbConnector.existsUserId(ud.id))
    //    complete(StatusCodes.NotFound, "User with id " + id + " does not exist.")
    // dbConnector.updateUserData(ud)
    complete(StatusCodes.OK, "User Data for " + ud.username + " has been updated.")
  }

  def isAuth(lr: LoginRequest) : Route = {
    checkCredentials(lr) match {
      case Error.OK =>
        complete(StatusCodes.OK)
      case Error.USER_NOT_FOUND =>
        complete(StatusCodes.NotFound)
      case Error.SERVER_ERROR =>
        complete(StatusCodes.InternalServerError)
      case Error.INVALID_PASSWORD =>
        complete(StatusCodes.Unauthorized)
    }
  }
}
