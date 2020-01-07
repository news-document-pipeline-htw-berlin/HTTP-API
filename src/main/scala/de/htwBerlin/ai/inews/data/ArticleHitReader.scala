package de.htwBerlin.ai.inews.data

import com.sksamuel.elastic4s.{Hit, HitReader}
import de.htwBerlin.ai.inews.core.Article

import scala.util.Try

object ArticleHitReader extends HitReader[Article] {
  override def read(hit: Hit): Try[Article] = {
    val newsSite = hit.sourceAsMap.getOrElse("news_site", hit.sourceAsMap.getOrElse("newsSite", "")).toString

    Try(Article(
      Option(hit.sourceAsMap("description").toString),
      newsSite,
      hit.sourceAsMap.getOrElse("title", "").toString,
      hit.sourceAsMap.getOrElse("text", "").toString,
      Option(hit.sourceAsMap("intro").toString),
      "", //hit.sourceAsMap("short_url").toString,
      hit.sourceAsMap.getOrElse("long_url", "").toString,
      hit.sourceAsMap.getOrElse("mongo_id", "").toString,
      hit.sourceAsMap.getOrElse("crawl_time", "").toString,
      Option(hit.sourceAsMap.getOrElse("published_time", "").toString)
    ))
  }
}
