package de.htwBerlin.ai.inews.http.routes

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import de.htwBerlin.ai.inews.core.JsonFormat._
import de.htwBerlin.ai.inews.core.ArticleQueryDTO
import de.htwBerlin.ai.inews.data.ArticleService

import scala.concurrent.ExecutionContext

class Articles()(implicit executionContext: ExecutionContext) {

  val route: Route = {
    pathPrefix("articles") {
      // /api/articles/{articleId}
      pathPrefix(Segment) { articleId =>
        complete(ArticleService.getById(articleId))
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
      get {
        parameters("offset" ? 10, "count" ? 20, "query" ? "", "department" ? "", "author" ? "") {
          (offset, count, query, department, author) => {
            val articleQuery = ArticleQueryDTO(offset, count, query, department, author)
            complete(ArticleService.getWithQuery(articleQuery))
          }
        }
      }
    }
  }
}
