package de.htwBerlin.ai.inews.http.routes

import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server._
import Directives._
import de.htwBerlin.ai.inews.core.Analytics.MostRelevantLemmas.LemmasNotFoundException
import de.htwBerlin.ai.inews.core.Analytics.TermOccurrence.TermNotFoundException
import org.joda.time.DateTime
import de.htwBerlin.ai.inews.core.Article.JsonFormat._
import de.htwBerlin.ai.inews.data.ArticleService

import scala.concurrent.ExecutionContext

class Analytics(articleService: ArticleService)(implicit executionContext: ExecutionContext) {
  implicit def analyticsExceptionHandler: ExceptionHandler =
    ExceptionHandler {
      case _: TermNotFoundException =>
        extractUri { uri =>
          complete(HttpResponse(NotFound, entity = "Term wasn't found. "))
        }
      case _: LemmasNotFoundException =>
        extractUri { uri =>
          complete(HttpResponse(NotFound, entity = "No Articles found. "))
        }
    }

  final val route: Route = Route.seal(
    pathPrefix("analytics") {
      // /api/analytics/lemmas
      pathPrefix("lemmas") {
        get(complete(articleService.getMostRelevantLemmas))
      } ~
      // /api/analytics/terms
      pathPrefix("terms") {
        get(
          parameters(
            "query".as[String],
            "timeFrom" ? DateTime.now().minusDays(30).getMillis,
            "timeTo" ? DateTime.now().getMillis
          ) { (query, timeFrom, timeTo) =>
            val analytics = articleService.getTermOccurrences(query, timeFrom, timeTo)
            complete(analytics)
          }
        )
      }
    }
  )
}
