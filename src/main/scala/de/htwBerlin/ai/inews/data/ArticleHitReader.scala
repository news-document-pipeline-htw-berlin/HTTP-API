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
      hit.sourceAsMap("title").toString,
      hit.sourceAsMap("text").toString,
      Option(hit.sourceAsMap("intro").toString),
      "", //hit.sourceAsMap("short_url").toString,
      hit.sourceAsMap("long_url").toString,
      hit.sourceAsMap("mongo_id").toString,
      hit.sourceAsMap("crawl_time").toString,
      Option(hit.sourceAsMap.getOrElse("published_time", "").toString)
    ))
  }
}
