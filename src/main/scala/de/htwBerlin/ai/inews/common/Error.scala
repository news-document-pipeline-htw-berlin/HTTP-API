package de.htwBerlin.ai.inews.common

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives.complete
import akka.http.scaladsl.server.{Directives, Route}
import de.htwBerlin.ai.inews.common.Error.{EMAIL_TAKEN, INVALID_PASSWORD, OK, PASSWORD_MISMATCH, SERVER_ERROR, USERNAME_TAKEN, USER_NOT_FOUND}

import scala.concurrent.ExecutionContext

/**
 * Defines several specialized client / server errors.
 */
object Error extends Enumeration {
  type Error = Value
  val OK, SERVER_ERROR, USER_NOT_FOUND, INVALID_PASSWORD, PASSWORD_MISMATCH, USERNAME_TAKEN, EMAIL_TAKEN
  = Value

  /**
   * Processes error code and returns a matching HTTP response.
   * @param e error code
   * @return HTTP response
   */
  def processError(e: Error.Value): Route = {
    e match {
      case OK =>
        complete(StatusCodes.OK)
      case SERVER_ERROR =>
        complete(StatusCodes.InternalServerError, "Internal Server Error.")
      case USER_NOT_FOUND =>
        complete(StatusCodes.NotFound, "User not found.")
      case INVALID_PASSWORD =>
        complete(StatusCodes.Unauthorized, "Invalid Password.")
      case PASSWORD_MISMATCH =>
        complete(StatusCodes.BadRequest, "Password mismatch.")
      case USERNAME_TAKEN =>
        complete(StatusCodes.BadRequest, "Username already taken.")
      case EMAIL_TAKEN =>
        complete(StatusCodes.BadRequest, "Email already in use.")
      case _ =>
        complete(StatusCodes.InternalServerError, "Internal Server Error.")
    }
  }
}
