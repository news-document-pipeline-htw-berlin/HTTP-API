package de.htwBerlin.ai.inews.http.routes

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import de.htwBerlin.ai.inews.core.ArticleDAO
import spray.json.DefaultJsonProtocol._

import scala.concurrent.ExecutionContext

class Articles()(implicit executionContext: ExecutionContext) {
  private val articleDAO = new ArticleDAO()(executionContext)

  val route: Route = {
    pathPrefix("articles") {
      pathPrefix("by-id") {
        get {
          parameter("id") { id =>
            // TODO this is only for testing
              onSuccess(articleDAO.fetchItem("test" + id + ".json"))(articles =>
                complete(articles)
              )
          }
        }
      } ~
      pathPrefix("trending") {
        get {
          complete("success")
        }
      } ~
      pathPrefix("search") {
        get {
          ???
        }
      }
    }
  }
}
