package de.htwBerlin.ai.inews.core

import spray.json.DefaultJsonProtocol._
import spray.json.RootJsonFormat

case class ArticleList(
  resultCount: Long,
  articles: Seq[Article]
)

object ArticleList {
  // this is needed to serialize the articleList to JSON
  implicit val articleListJsonFormat: RootJsonFormat[ArticleList] =
    jsonFormat2(ArticleList.apply)
}
