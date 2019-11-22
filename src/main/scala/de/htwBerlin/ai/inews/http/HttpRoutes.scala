package de.htwBerlin.ai.inews.http

import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.Directives._
import de.htwBerlin.ai.inews.http.routes.Articles

import scala.concurrent.ExecutionContext

class HttpRoutes(implicit executionContext: ExecutionContext) {
  val articleRoute = new Articles()(executionContext)

  val route: Route =
    pathPrefix("api") {
      articleRoute.route
    }
}
