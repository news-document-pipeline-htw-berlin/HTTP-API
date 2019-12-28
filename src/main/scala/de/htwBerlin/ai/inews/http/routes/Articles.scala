package de.htwBerlin.ai.inews.http.routes

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import de.htwBerlin.ai.inews.core.JsonFormat._
import de.htwBerlin.ai.inews.data.{ArticleQueryDTO, ArticleService}

import scala.concurrent.ExecutionContext

class Articles()(implicit executionContext: ExecutionContext) {

  final val route: Route = {
    pathPrefix("articles") {
      // /api/articles/{articleId}
      pathPrefix(Segment) { articleId =>
        get {
          complete(ArticleService.getById(articleId))
        }
      } ~
      // /api/articles/newspapers
      pathPrefix("newspapers") {
        get {
          complete(ArticleService.getNewspapers)
        }
      } ~
      // /api/articles/authors
      pathPrefix("authors") {
        get {
          parameters("query".?) { query =>
            complete(ArticleService.getAuthors(query))
          }
        }
      } ~
      // /api/articles
      get(
        parameters(
          "offset" ? 0,
          "count" ? 20,
          "query" ? "",
          "department" ? "",
          "author" ? ""
        ) { (offset, count, query, department, author) => {
            val articleQuery = ArticleQueryDTO(offset, count, query, department, author)
            val articles = ArticleService.getWithQuery(articleQuery)

            complete(articles)
          }
        }
      )
    }
  }
}
