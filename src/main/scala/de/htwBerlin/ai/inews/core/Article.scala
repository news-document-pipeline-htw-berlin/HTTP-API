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
  implicit val articleJsonFormat: RootJsonFormat[Article] =
    jsonFormat8(Article.apply)
}
