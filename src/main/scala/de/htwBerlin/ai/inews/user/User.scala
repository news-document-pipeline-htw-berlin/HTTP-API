package de.htwBerlin.ai.inews.user

import org.mongodb.scala.bson.ObjectId
import scala.collection.immutable
import scala.util.{Success, Try}

case class User(
                 _id: ObjectId,
                 username: String,
                 email: String,
                 password: String,
                 suggestions: Boolean,
                 darkMode: Boolean,
                 keywords: immutable.Map[String, Int])
case class UserData(
                     _id: String,
                     username: String,
                     email: String,
                     password: String,
                     suggestions: Boolean,
                     darkMode: Boolean
                   )
case class LoginRequest(
                         user: String,
                         password: String,
                         rememberMe: Boolean )

case class AuthRequest(
                        user: String,
                        password: String)

case class SignUpRequest(
                          username: String,
                          email: String,
                          password: String,
                          password_rep: String)

case class ChangePasswordRequest(
                                  user: String,
                                  oldPW: String,
                                  newPW: String,
                                  repPW: String)

case class KeyWords(list: List[String])
case class UserSuggestions(_id: String)