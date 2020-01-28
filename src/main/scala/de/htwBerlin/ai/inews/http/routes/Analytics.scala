package de.htwBerlin.ai.inews.http.routes

import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server._
import Directives._
import org.joda.time.DateTime

import de.htwBerlin.ai.inews.core.JsonFormat._
import de.htwBerlin.ai.inews.data.ArticleService
import de.htwBerlin.ai.inews.core.Analytics.TermNotFoundException

import scala.concurrent.ExecutionContext

class Analytics(articleService: ArticleService)(implicit executionContext: ExecutionContext) {
  implicit def myExceptionHandler: ExceptionHandler =
    ExceptionHandler {
      case _: TermNotFoundException =>
        extractUri { uri =>
          complete(HttpResponse(NotFound, entity = "Term is not present in any article. "))
        }
    }

  final val route: Route = Route.seal(
    // /api/analytics
    pathPrefix("analytics") {
      get(
        parameters(
          "query".as[String],
          "timeFrom" ? 0L,
          "timeTo" ? DateTime.now().getMillis
        ) { (query, timeFrom, timeTo) =>
          val analytics = articleService.getAnalytics(query, timeFrom, timeTo)
          complete(analytics)
        }
      )
    }
  )
}
