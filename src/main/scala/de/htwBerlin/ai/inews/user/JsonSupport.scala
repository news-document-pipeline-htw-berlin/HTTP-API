package de.htwBerlin.ai.inews.user

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.{DefaultJsonProtocol, RootJsonFormat}

final case class LoginRequest(user: String, password: String, rememberMe: Boolean)
final case class AuthRequest(user: String, password: String)
final case class SignUpRequest(username: String, email: String, password: String, password_rep: String)
final case class ChangePasswordRequest(user: String, oldPW: String, newPW: String, repPW: String)
final case class UserData(_id: String, username: String, email: String, password: String,
                          suggestions: Boolean, darkMode: Boolean)

trait JsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val loginRequestFormat: RootJsonFormat[LoginRequest] = jsonFormat3(LoginRequest)
  implicit val authRequestFormat: RootJsonFormat[AuthRequest] = jsonFormat2(AuthRequest)
  implicit val signUpRequestFormat: RootJsonFormat[SignUpRequest] = jsonFormat4(SignUpRequest)
  implicit val userDataFormat: RootJsonFormat[UserData] = jsonFormat6(UserData)
  implicit val changePasswordRequestFormat: RootJsonFormat[ChangePasswordRequest] = jsonFormat4(ChangePasswordRequest)
}
