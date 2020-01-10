package de.htwBerlin.ai.inews.http.routes

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import de.htwBerlin.ai.inews.core.JsonFormat._
import de.htwBerlin.ai.inews.data.{ArticleQueryDTO, ArticleService}

import scala.concurrent.ExecutionContext

class Articles()(implicit executionContext: ExecutionContext) {

  private val articleService = new ArticleService()(executionContext)

  final val route: Route = {
    pathPrefix("articles") {
      // /api/articles/newspapers
      pathPrefix("newspapers") {
        get {
          complete(articleService.getNewspapers)
        }
      } ~
      // /api/articles/authors
      pathPrefix("authors") {
        get {
          parameters("query".?) { query =>
            complete(articleService.getAuthors(query))
          }
        }
      } ~
      // /api/articles/{articleId}
      pathPrefix(Segment) { articleId =>
        get {
          complete(articleService.getById(articleId))
        }
      } ~
      // /api/articles
      get(
        parameters(
          "offset" ? 0,
          "count" ? 20,
          "query".?,
          "department".as[String].*,
          "newspaper".as[String].*,
          "author".?,
        ) { (offset, count, query, departments, newspapers, author) => {
            val articleQuery = ArticleQueryDTO(offset, count, query, departments, newspapers, author)
            val articles = articleService.getWithQuery(articleQuery)

            complete(articles)
          }
        }
      )
    }
  }
}
