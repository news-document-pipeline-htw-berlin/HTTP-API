package de.htwBerlin.ai.inews.http

import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.Directives._
import de.htwBerlin.ai.inews.http.routes.Articles
import ch.megard.akka.http.cors.scaladsl.CorsDirectives._

import scala.concurrent.ExecutionContext

class HttpRoutes(implicit executionContext: ExecutionContext) {
  private val articleRoute = new Articles()(executionContext)

  val route: Route = cors() {
    pathPrefix("api") {
      articleRoute.route
    }
  }
}
