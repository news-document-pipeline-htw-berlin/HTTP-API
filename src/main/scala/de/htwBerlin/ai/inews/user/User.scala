package de.htwBerlin.ai.inews.user

import org.mongodb.scala.bson.ObjectId

import scala.collection.immutable

/**
 * Representation of user entity.
 */
case class User(
                 _id: ObjectId,
                 username: String,
                 email: String,
                 password: String,
                 suggestions: Boolean,
                 darkMode: Boolean,
                 keywords: immutable.Map[String, Int])

/**
 * Representation of user data request / response body.
 */
case class UserData(
                     _id: String,
                     username: String,
                     email: String,
                     password: String,
                     suggestions: Boolean,
                     darkMode: Boolean
                   )

/**
 * Representation of login request body.
 */
case class LoginRequest(
                         user: String,
                         password: String,
                         rememberMe: Boolean)

/**
 * Representation of auth request body.
 */
case class AuthRequest(
                        user: String,
                        password: String)

/**
 * Representation of signup request body.
 */
case class SignUpRequest(
                          username: String,
                          email: String,
                          password: String,
                          password_rep: String)

/**
 * Representation of change password request body.
 */
case class ChangePasswordRequest(
                                  user: String,
                                  oldPW: String,
                                  newPW: String,
                                  repPW: String)

/**
 * Representation of keywords request / response body.
 */
case class KeyWords(list: List[String])

/**
 * Representation of user suggestions request / response body.
 */
case class UserSuggestions(_id: String)