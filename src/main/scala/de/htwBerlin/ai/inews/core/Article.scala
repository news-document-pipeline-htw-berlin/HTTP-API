package de.htwBerlin.ai.inews.core

import spray.json.DefaultJsonProtocol._
import spray.json.RootJsonFormat

case class Article(
  title: String,
  intro: String,
  article: String,
  author: String,
  published: String,
  summery: String,
  url: String,
  department: String
)

object Article {
  // this is needed to serialize the article to JSON
  implicit val articleJsonFormat: RootJsonFormat[Article] =
    // the 8 stands for 8 parameters of class Article
    jsonFormat8(Article.apply)
}
