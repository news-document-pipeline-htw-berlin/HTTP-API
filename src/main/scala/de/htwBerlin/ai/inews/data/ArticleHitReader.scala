package de.htwBerlin.ai.inews.data

import com.sksamuel.elastic4s.{Hit, HitReader}
import de.htwBerlin.ai.inews.core.Article
import org.joda.time.DateTime

import scala.util.Try

object ArticleHitReader extends HitReader[Article] {
  override def read(hit: Hit): Try[Article] = {

    Try(Article(
      hit.id,
      hit.sourceAsMap("authors").asInstanceOf[List[String]],
      hit.sourceAsMap("crawlTime") match {
        case time: String => DateTime.parse(time).getMillis
        case _ => 0
      },
      hit.sourceAsMap("departments").asInstanceOf[List[String]],
      hit.sourceAsMap("description").toString,
      hit.sourceAsMap("imageLinks").asInstanceOf[List[String]],
      hit.sourceAsMap("intro").toString,
      hit.sourceAsMap("keywords").asInstanceOf[List[String]],
      hit.sourceAsMap("lemmas").asInstanceOf[List[String]],
      hit.sourceAsMap("links").asInstanceOf[List[String]],
      hit.sourceAsMap("longUrl").toString,
      hit.sourceAsMap("mostRelevantLemmas").asInstanceOf[List[String]],
      hit.sourceAsMap("newsSite").toString,
      hit.sourceAsMap("publishedTime") match {
        case time: String => DateTime.parse(time).getMillis
        case _ => 0
      },
      hit.sourceAsMap("readingTime").asInstanceOf[Int],
      hit.sourceAsMap("text").toString,
      hit.sourceAsMap("title").toString
    ))
  }
}
