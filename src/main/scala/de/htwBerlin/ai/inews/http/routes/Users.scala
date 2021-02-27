package de.htwBerlin.ai.inews.http.routes

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.{Directives, Route}
import de.htwBerlin.ai.inews.core.Article.JsonFormat._
import de.htwBerlin.ai.inews.common.JWT
import de.htwBerlin.ai.inews.user.{AuthRequest, ChangePasswordRequest, JsonSupport, KeyWords, LoginRequest, SignUpRequest, UserData, UserService}
import scala.concurrent.ExecutionContext

/**
 * Defines routes associated with users.
 *
 * @param userService
 * @param executionContext
 */
class Users(userService: UserService)(implicit executionContext: ExecutionContext) extends Directives with JsonSupport {

  final val route: Route = {
    pathPrefix("users") {
      pathPrefix("account") {
        // /api/users/account?account={account}
        // DELETE user account (requires AuthRequest and JWT)
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
          // /api/users/account?data={data}
          // DELETE user data (requires AuthRequest and JWT)
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
          // /api/users/account
          // GET user data (requires JWT)
          get {
            JWT.authenticated { claims =>
              userService.getUserData(claims("id").toString)
            }
          } ~
          // /api/users/account
          // PUT new password (requires ChangePasswordRequest and JWT)
          put {
            JWT.authenticated { claims =>
              entity(as[ChangePasswordRequest]) {
                cpr => userService.updatePassword(cpr)
              }
            }
          } ~
          // /api/users/account
          // PUT new user data (requires UserData and JWT)
          put {
            JWT.authenticated { claims =>
              entity(as[UserData]) {
                ud => userService.updateUserData(ud, claims("id").toString)
              }
            }
          }
      } ~
        // /api/users/keywords
        // GET user keyword count (requires JWT)
        pathPrefix("keywords") {
          JWT.authenticated { claims =>
            userService.getKeywordCount(claims("id").toString)
          }
        } ~
        pathPrefix("suggestions") {
          // /api/users/suggestions
          // PUT keywords (requires KeyWords and JWT)
          put {
            JWT.authenticated { claims =>
              entity(as[KeyWords]) {
                k => userService.updateKeywords(claims("id").toString, k)
              }

            }
          } ~
            // /api/users/suggestions?offset={offset}&count={count}
            // GET suggestions (requires JWT)
            get(
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
        pathPrefix("login") {
          // /api/users/login
          // POST login data (requires LoginRequest)
          post {
            entity(as[LoginRequest]) {
              lr => userService.handleLogin(lr)
            }
          }
        } ~
        pathPrefix("signup") {
          // /api/users/signup
          // POST signup data (requires SignUpRequest)
          post {
            entity(as[SignUpRequest]) {
              sur => userService.handleSignUp(sur)
            }
          }
        }
    }
  }
}

