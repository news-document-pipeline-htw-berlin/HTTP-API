package de.htwBerlin.ai.inews.core.Article

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import de.htwBerlin.ai.inews.core.Analytics.MostRelevantLemmas.{Lemma, Lemmas}
import de.htwBerlin.ai.inews.core.Analytics.TermOccurrence.{TermOccurrence, TermOccurrences}
import spray.json.{DefaultJsonProtocol, RootJsonFormat}

object JsonFormat extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val articleJsonFormat: RootJsonFormat[Article] =
    jsonFormat21(Article.apply)

  implicit val articleListJsonFormat: RootJsonFormat[ArticleList] =
    jsonFormat2(ArticleList)

  implicit val termOccurrenceJsonFormat: RootJsonFormat[TermOccurrence] =
    jsonFormat3(TermOccurrence)

  implicit val termOccurrencesJsonFormat: RootJsonFormat[TermOccurrences] =
    jsonFormat5(TermOccurrences)

  implicit val lemmaJsonFormat: RootJsonFormat[Lemma] =
    jsonFormat2(Lemma)

  implicit val lemmasJsonFormat: RootJsonFormat[Lemmas] =
    jsonFormat1(Lemmas)
}
