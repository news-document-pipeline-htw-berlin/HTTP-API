package de.htwBerlin.ai.inews.http.routes

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import de.htwBerlin.ai.inews.core.JsonFormat._
import de.htwBerlin.ai.inews.data.{ArticleQueryDTO, ArticleService}
import de.htwBerlin.ai.inews.http.handler.CORSHandler

import scala.concurrent.ExecutionContext

class Articles()(implicit executionContext: ExecutionContext) {

  // to make this API accessible from different domains (or different ports on the same domain) we need to use CORSHandler
  // the CORSHandler adds multiple headers (especially 'Access-Control-Allow-Origin') to requests from different domains, as well as an OPTION route
  // every (GET-)route that should be reachable from different domains needs to be initialized with getWithCors and completed with completeWithCors

  val route: Route = {
    pathPrefix("articles") {
      // /api/articles/{articleId}
      pathPrefix(Segment) { articleId =>
        CORSHandler.getWithCors {
          CORSHandler.completeWithCors(ArticleService.getById(articleId))
        }
      } ~
      // /api/articles/newspapers
      pathPrefix("newspapers") {
        get {
          ???
        }
      } ~
      // /api/articles/authors
      pathPrefix("authors") {
        get {
          parameters("query".?) { query =>
            ???
          }
        }
      } ~
      // /api/articles
      CORSHandler.getWithCors {
        parameters(
          "offset" ? 0,
          "count" ? 20,
          "query" ? "",
          "department" ? "",
          "author" ? ""
        ) { (offset, count, query, department, author) => {
            val articleQuery = ArticleQueryDTO(offset, count, query, department, author)
            val articles = ArticleService.getWithQuery(articleQuery)

            CORSHandler.completeWithCors(articles)
          }
        }
      }
    }
  }
}
