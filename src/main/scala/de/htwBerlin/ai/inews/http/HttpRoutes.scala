package de.htwBerlin.ai.inews.http

import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.Directives._
import de.htwBerlin.ai.inews.http.routes.{Analytics, Articles, Users}
import ch.megard.akka.http.cors.scaladsl.CorsDirectives._
import de.htwBerlin.ai.inews.data.ArticleService
import de.htwBerlin.ai.inews.user.UserService

import scala.concurrent.ExecutionContext

class HttpRoutes(implicit executionContext: ExecutionContext) {

  // TODO dependency injection
  private val articleService = new ArticleService()(executionContext)
  private val userService = new UserService()(executionContext)
  private val articleRoute = new Articles(articleService)(executionContext)
  private val analyticsRoute = new Analytics(articleService)(executionContext)
  private val usersRoute = new Users(userService)(executionContext)

  val route: Route = cors() {
    pathPrefix("api") {
      usersRoute.route ~
      articleRoute.route ~
      analyticsRoute.route
    }
  }
}
