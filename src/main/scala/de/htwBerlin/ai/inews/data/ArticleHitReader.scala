package de.htwBerlin.ai.inews.data

import com.sksamuel.elastic4s.{Hit, HitReader}
import de.htwBerlin.ai.inews.core.Article

import scala.util.Try

class ArticleHitReader extends HitReader[Article] {
  override def read(hit: Hit): Try[Article] = {
    Try(Article(
      Option(hit.sourceAsMap("description").toString),
      hit.sourceAsMap("news_site").toString,
      hit.sourceAsMap("title").toString,
      hit.sourceAsMap("text").toString,
      Option(hit.sourceAsMap("intro").toString),
      hit.sourceAsMap("short_url").toString,
      hit.sourceAsMap("long_url").toString,
      hit.sourceAsMap("mongo_id").toString,
      hit.sourceAsMap("crawl_time").toString,
      Option(hit.sourceAsMap("published_time").toString)
    ))
  }
}
