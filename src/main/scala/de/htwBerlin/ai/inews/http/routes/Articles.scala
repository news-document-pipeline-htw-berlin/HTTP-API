package de.htwBerlin.ai.inews.http.routes

import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{RequestContext, Route, RouteResult}
import de.htwBerlin.ai.inews.core.JsonFormat._
import de.htwBerlin.ai.inews.data.{ArticleQueryDTO, ArticleService}
import de.htwBerlin.ai.inews.http.CORSHandler

import scala.concurrent.{ExecutionContext, Future}

class Articles()(implicit executionContext: ExecutionContext) {

  // this is needed to add headers to requests from different domains and avoid CORS-errors
  // every route that should be reachable from a different domain needs to be initialized with getWithCors
  // and completed with completeWithCors
  private val corsHandler = new CORSHandler {}

  val route: Route = {
    pathPrefix("articles") {
      // /api/articles/{articleId}
      pathPrefix(Segment) { articleId =>
        getWithCors {
          completeWithCors(ArticleService.getById(articleId))
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
      getWithCors {
        parameters(
          "offset" ? 0,
          "count" ? 20,
          "query" ? "",
          "department" ? "",
          "author" ? ""
        ) { (offset, count, query, department, author) => {
            val articleQuery = ArticleQueryDTO(offset, count, query, department, author)
            completeWithCors(ArticleService.getWithQuery(articleQuery))
          }
        }
      }
    }
  }

  def getWithCors(route: Route): RequestContext => Future[RouteResult] = {
    options {
      corsHandler.corsHandler(complete(StatusCodes.OK))
    } ~ get {
      route
    }
  }

  def completeWithCors(content: ToResponseMarshallable): RequestContext => Future[RouteResult] = {
    corsHandler.corsHandler(complete(content))
  }
}
