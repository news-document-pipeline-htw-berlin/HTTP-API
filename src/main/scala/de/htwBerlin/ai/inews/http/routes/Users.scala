package de.htwBerlin.ai.inews.http.routes

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives.{parameters, pathPrefix, post, _}
import akka.http.scaladsl.server.{Directives, Route}
import de.htwBerlin.ai.inews.user.{JsonSupport, LoginRequest, SignUpRequest, UserData, UserService}

import scala.concurrent.ExecutionContext


class Users(userService: UserService)(implicit executionContext: ExecutionContext) extends Directives with JsonSupport {


  final val route: Route = {
    pathPrefix("users") {
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
    } ~
      // TODO: this route requires auth
      get {
        // TODO: get parameter from private claim in JWT
        parameters(
          "id".as[Long]
        ) {
          id => userService.getUserData(id)
        }
      } ~
      // TODO: this route requires auth
      put {
        entity(as[UserData]) {
          ud => userService.updateUserData(ud)
        }
      }
  }
}

