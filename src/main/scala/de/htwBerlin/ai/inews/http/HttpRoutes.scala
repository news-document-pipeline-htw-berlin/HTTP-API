package de.htwBerlin.ai.inews.http

import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.Directives._
import de.htwBerlin.ai.inews.http.routes.{Analytics, Articles}
import ch.megard.akka.http.cors.scaladsl.CorsDirectives._
import de.htwBerlin.ai.inews.data.ArticleService

import scala.concurrent.ExecutionContext

class HttpRoutes(implicit executionContext: ExecutionContext) {

  // TODO dependency injection
  private val articleService = new ArticleService()(executionContext)
  private val articleRoute = new Articles(articleService)(executionContext)
  private val analyticsRoute = new Analytics(articleService)(executionContext)

  val route: Route = cors() {
    pathPrefix("api") {
      articleRoute.route ~
      analyticsRoute.route
    }
  }
}
