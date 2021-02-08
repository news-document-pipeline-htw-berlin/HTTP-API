package de.htwBerlin.ai.inews.user

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.{DefaultJsonProtocol, RootJsonFormat}

case class LoginRequest(user: String, password: String, rememberMe: Boolean)
case class AuthRequest(user: String, password: String)
case class SignUpRequest(username: String, email: String, password: String, password_rep: String)
case class ChangePasswordRequest(user: String, oldPW: String, newPW: String, repPW: String)
case class UserData(_id: String, username: String, email: String, password: String,
                          suggestions: Boolean, darkMode: Boolean, keywords: scala.collection.immutable.Map[String, Int])
case class KeyWords(list: List[String])
case class UserSuggestions(_id: String)

trait JsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val loginRequestFormat: RootJsonFormat[LoginRequest] = jsonFormat3(LoginRequest)
  implicit val authRequestFormat: RootJsonFormat[AuthRequest] = jsonFormat2(AuthRequest)
  implicit val signUpRequestFormat: RootJsonFormat[SignUpRequest] = jsonFormat4(SignUpRequest)
  implicit val userDataFormat: RootJsonFormat[UserData] = jsonFormat7(UserData)
  implicit val changePasswordRequestFormat: RootJsonFormat[ChangePasswordRequest] = jsonFormat4(ChangePasswordRequest)
  implicit val keywordsFormat: RootJsonFormat[KeyWords] = jsonFormat1(KeyWords)
  implicit val userSuggestionsFormat: RootJsonFormat[UserSuggestions] = jsonFormat1(UserSuggestions)
}