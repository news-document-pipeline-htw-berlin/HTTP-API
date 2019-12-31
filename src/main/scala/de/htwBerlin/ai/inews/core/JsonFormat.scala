package de.htwBerlin.ai.inews.core

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.{DefaultJsonProtocol, RootJsonFormat}

object JsonFormat extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val articleJsonFormat: RootJsonFormat[Article] =
    // the 10 stands for 10 parameters of class Article
    jsonFormat10(Article.apply)

  implicit val articleListJsonFormat: RootJsonFormat[ArticleList] =
    jsonFormat2(ArticleList.apply)
}
