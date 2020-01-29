package de.htwBerlin.ai.inews.core

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import de.htwBerlin.ai.inews.core.Analytics.MostRelevantLemmas._
import de.htwBerlin.ai.inews.core.Analytics.TermOccurrence._
import de.htwBerlin.ai.inews.core.Article.{Article, ArticleList}
import spray.json.{DefaultJsonProtocol, RootJsonFormat}

object JsonFormat extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val articleJsonFormat: RootJsonFormat[Article] =
    jsonFormat17(Article.apply)

  implicit val articleListJsonFormat: RootJsonFormat[ArticleList] =
    jsonFormat2(ArticleList.apply)

  implicit val termOccurrenceJsonFormat: RootJsonFormat[TermOccurrence] =
    jsonFormat3(TermOccurrence.apply)

  implicit val termOccurrencesJsonFormat: RootJsonFormat[TermOccurrences] =
    jsonFormat5(TermOccurrences.apply)

  implicit val lemmaJsonFormat: RootJsonFormat[Lemma] =
    jsonFormat2(Lemma.apply)

  implicit val lemmasJsonFormat: RootJsonFormat[Lemmas] =
    jsonFormat1(Lemmas.apply)
}
