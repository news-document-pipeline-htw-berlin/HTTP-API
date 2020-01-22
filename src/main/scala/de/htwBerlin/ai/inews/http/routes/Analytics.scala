package de.htwBerlin.ai.inews.http.routes

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import de.htwBerlin.ai.inews.core.JsonFormat._
import de.htwBerlin.ai.inews.data.ArticleService
import org.joda.time.DateTime

import scala.concurrent.ExecutionContext

class Analytics(articleService: ArticleService)(implicit executionContext: ExecutionContext) {
  final val route: Route = {
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
  }
}
