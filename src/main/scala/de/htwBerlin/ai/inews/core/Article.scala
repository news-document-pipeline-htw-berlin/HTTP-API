package de.htwBerlin.ai.inews.core

import spray.json.DefaultJsonProtocol._
import spray.json.RootJsonFormat

case class Article(
  description: Option[String],
  news_site: String,
  title: String,
  text: String,
  intro: Option[String],
  short_url: String,
  long_url: String,
  mongo_id: String,
  crawl_time: String,
  published_time: Option[String],
)

object Article {
  // this is needed to serialize the article to JSON
  implicit val articleJsonFormat: RootJsonFormat[Article] =
  // the 8 stands for 8 parameters of class Article
    jsonFormat10(Article.apply)
}
