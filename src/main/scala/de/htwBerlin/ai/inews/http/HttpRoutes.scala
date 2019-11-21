package de.htwBerlin.ai.inews.http

import akka.http.scaladsl.server.Route
import de.htwBerlin.ai.inews.http.routes.ArticleTest

import scala.concurrent.ExecutionContext

class HttpRoutes(implicit executionContext: ExecutionContext) {
  val articleRoute = new ArticleTest()(executionContext)

  val route: Route = articleRoute.route
}
