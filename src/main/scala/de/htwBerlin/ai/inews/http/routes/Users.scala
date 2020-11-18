package de.htwBerlin.ai.inews.http.routes

import akka.http.scaladsl.server.Directives.{pathPrefix, post}
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.Directives._
import de.htwBerlin.ai.inews.user.UserService

import scala.concurrent.ExecutionContext


class Users(userService: UserService)(implicit executionContext: ExecutionContext) {


  final val route: Route = {
    // /api/users/login
    pathPrefix("users") {
      pathPrefix("login") {
        post {
          userService.handleLogin()
        }
      }
    }
  }
}
