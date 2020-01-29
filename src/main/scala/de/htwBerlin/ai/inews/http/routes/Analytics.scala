package de.htwBerlin.ai.inews.http.routes

import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server._
import Directives._
import de.htwBerlin.ai.inews.core.Analytics.TermOccurrence.TermNotFoundException
import org.joda.time.DateTime
import de.htwBerlin.ai.inews.core.JsonFormat._
import de.htwBerlin.ai.inews.data.ArticleService

import scala.concurrent.ExecutionContext

class Analytics(articleService: ArticleService)(implicit executionContext: ExecutionContext) {
  implicit def analyticsExceptionHandler: ExceptionHandler =
    ExceptionHandler {
      case _: TermNotFoundException =>
        extractUri { uri =>
          complete(HttpResponse(NotFound, entity = "Term wasn't found. "))
        }
    }

  final val route: Route = Route.seal(
    pathPrefix("analytics") {
      // /api/analytics/lemmas
      pathPrefix("lemmas") {
        get(complete(articleService.getMostRelevantLemmas()))
      } ~
      // /api/analytics
      get(
        parameters(
          "query".as[String],
          "timeFrom" ? 0L,
          "timeTo" ? DateTime.now().getMillis
        ) { (query, timeFrom, timeTo) =>
          val analytics = articleService.getTermOccurrences(query, timeFrom, timeTo)
          complete(analytics)
        }
      )
    }
  )
}
