package de.htwBerlin.ai.inews.core

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import de.htwBerlin.ai.inews.core.Analytics.{Analytics, TermOccurrence}
import spray.json.{DefaultJsonProtocol, RootJsonFormat}

object JsonFormat extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val articleJsonFormat: RootJsonFormat[Article] =
    jsonFormat17(Article.apply)

  implicit val articleListJsonFormat: RootJsonFormat[ArticleList] =
    jsonFormat2(ArticleList.apply)

  implicit val termOccurrenceJsonFormat: RootJsonFormat[TermOccurrence] =
    jsonFormat3(TermOccurrence.apply)

  implicit val analyticsJsonFormat: RootJsonFormat[Analytics] =
    jsonFormat5(Analytics.apply)
}
