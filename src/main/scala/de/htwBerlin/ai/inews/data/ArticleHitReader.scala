package de.htwBerlin.ai.inews.data

import com.sksamuel.elastic4s.{Hit, HitReader}
import de.htwBerlin.ai.inews.core.Article
import org.joda.time.DateTime

import scala.util.Try

object ArticleHitReader extends HitReader[Article] {
  override def read(hit: Hit): Try[Article] = {
    val publishedTime = hit.sourceAsMap("published_time") match {
      case Some(time: String) => DateTime.parse(time).getMillis
      case _ => 0
    }

    Try(Article(
      hit.id,
      hit.sourceAsMap("authors").asInstanceOf[List[String]],
      DateTime.parse(hit.sourceAsMap("crawl_time").toString).getMillis,
      Seq(), // TODO departments
      hit.sourceAsMap("description").toString,
      hit.sourceAsMap("image_links").asInstanceOf[List[String]],
      hit.sourceAsMap("intro").toString,
      hit.sourceAsMap("keywords").asInstanceOf[List[String]],
      hit.sourceAsMap("lemmas").asInstanceOf[List[String]],
      hit.sourceAsMap("links").asInstanceOf[List[String]],
      hit.sourceAsMap("long_url").toString,
      Seq(), // TODO mostRelevantLemmas
      hit.sourceAsMap("newsSite").toString,
      publishedTime,
      hit.sourceAsMap("reading_time").asInstanceOf[Int],
      hit.sourceAsMap("text").toString,
      hit.sourceAsMap("title").toString
    ))
  }
}
