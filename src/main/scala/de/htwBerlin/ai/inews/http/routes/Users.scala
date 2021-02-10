package de.htwBerlin.ai.inews.http.routes

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives.{parameters, pathPrefix, post, _}
import akka.http.scaladsl.server.{Directives, Route}
import de.htwBerlin.ai.inews.core.Article.JsonFormat._
import de.htwBerlin.ai.inews.common.JWT
import de.htwBerlin.ai.inews.user.{AuthRequest, ChangePasswordRequest, JsonSupport, KeyWords, LoginRequest, SignUpRequest, User, UserData, UserService}
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._

import scala.concurrent.ExecutionContext


class Users(userService: UserService)(implicit executionContext: ExecutionContext) extends Directives with JsonSupport {
  final val route: Route = {
    pathPrefix("users") {
      // /api/users/account
      pathPrefix("account") {
        delete {
          JWT.authenticated { claims =>
            parameters(
              "account".as[Boolean]
            ) {
              account =>
                entity(as[AuthRequest]) {
                  ar =>
                    if (account) {
                      userService.deleteUser(ar)
                    } else {
                      complete(StatusCodes.NotFound)
                    }
                }
            }
          }
        } ~
          delete {
            JWT.authenticated { claims =>
              parameters(
                "data".as[Boolean]
              ) {
                data =>
                  entity(as[AuthRequest]) {
                    ar =>
                      if (data) {
                        userService.deleteData(ar)
                      } else {
                        complete(StatusCodes.NotFound)
                      }
                  }
              }
            }
          } ~
          get {
            JWT.authenticated { claims =>
              userService.getUserData(claims("id").toString)
            }
          } ~
            put {
              JWT.authenticated { claims =>
                entity(as[ChangePasswordRequest]) {
                  cpr => userService.updatePassword(cpr)
                }
              }
            } ~
          put {
            JWT.authenticated { claims =>
              entity(as[UserData]) {
                ud => userService.updateUserData(ud, claims("id").toString)
              }
            }
          }
      } ~
        pathPrefix("keywords") {
          JWT.authenticated { claims =>
            userService.getKeywords(claims("id").toString)
          }
        } ~
        pathPrefix("suggestions") {
          put {
            JWT.authenticated { claims =>
              entity(as[KeyWords]) {
                k => userService.updateKeywords(claims("id").toString, k)
              }

            }
          } ~
            get (
              JWT.authenticated { claims =>
              parameters(
                "offset" ? 0,
                "count" ? 20
              ) {
                (offset, count) => {
                    val suggestions = userService.getSuggestionsByKeywords(claims("id").toString, offset, count)
                    complete(suggestions)
                  }
                }
              }
            )
        } ~
        // /api/users/login
        pathPrefix("login") {
          post {
            entity(as[LoginRequest]) {
              lr => userService.handleLogin(lr)
            }
          }
        } ~
        // /api/users/signup
        pathPrefix("signup") {
          post {
            entity(as[SignUpRequest]) {
              sur => userService.handleSignUp(sur)
            }
          }
        } ~
        // /api/users/auth
        pathPrefix("auth") {
          post {
            entity(as[LoginRequest]) {
              lr => userService.isAuth(lr)
            }
          }
        }
    }
  }
}

