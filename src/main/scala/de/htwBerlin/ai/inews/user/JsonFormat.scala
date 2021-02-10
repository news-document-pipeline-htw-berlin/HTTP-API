package de.htwBerlin.ai.inews.user

import de.htwBerlin.ai.inews.common.AnyJsonFormat
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import de.htwBerlin.ai.inews.common.MongoDBProtocol.ObjectIdSerializer
import org.mongodb.scala.bson.ObjectId
import spray.json.{DefaultJsonProtocol, JsFalse, JsNumber, JsString, JsTrue, JsValue, JsonFormat, RootJsonFormat}


trait JsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val keyWordsJsonFormat: RootJsonFormat[KeyWords] =
    jsonFormat1(KeyWords)
  implicit val userJsonFormat: RootJsonFormat[User] =
    jsonFormat7(User)
  implicit val userDataJsonFormat: RootJsonFormat[UserData] =
    jsonFormat6(UserData)
  implicit val loginRequestJsonFormat: RootJsonFormat[LoginRequest] =
    jsonFormat3(LoginRequest)
  implicit val authRequestJsonFormat: RootJsonFormat[AuthRequest] =
    jsonFormat2(AuthRequest)
  implicit val signUpRequestJsonFormat: RootJsonFormat[SignUpRequest] =
    jsonFormat4(SignUpRequest)
  implicit val changePasswordRequestJsonFormat: RootJsonFormat[ChangePasswordRequest] =
    jsonFormat4(ChangePasswordRequest)
}