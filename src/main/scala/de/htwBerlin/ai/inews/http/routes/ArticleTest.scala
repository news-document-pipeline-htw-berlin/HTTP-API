package de.htwBerlin.ai.inews.http.routes

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.server.Directives._
import de.htwBerlin.ai.inews.core.{Article, ArticleDAO}
import de.htwBerlin.ai.inews.util.JSONParser
import spray.json.DefaultJsonProtocol._

import scala.concurrent.{ExecutionContext, Future}

class ArticleTest()(implicit executionContext: ExecutionContext) {

  private val articleDAO = new ArticleDAO()(executionContext)

  val route = {
    pathPrefix("api") {
      pathPrefix("articles") {
        get {
          parameter("q".?) { // optional parameter (.?)
            case Some(query) =>
              // query parameter supplied
              // not used atm
              onSuccess(articleDAO.fetchItem("test2.json"))(articles =>
                complete(articles)
              )
            case None =>
              // no query param supplied
              onSuccess(articleDAO.fetchItem("test.json"))(articles =>
                complete(articles)
              )
          }
        }
      }
    }
  }
}
