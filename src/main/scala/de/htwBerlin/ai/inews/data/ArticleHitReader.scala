package de.htwBerlin.ai.inews.data

import com.sksamuel.elastic4s.{Hit, HitReader}
import de.htwBerlin.ai.inews.core.Article.Article
import org.joda.time.DateTime

import scala.util.Try

object ArticleHitReader extends HitReader[Article] {
  override def read(hit: Hit): Try[Article] = {
    Try(Article(
      hit.id,
      hit.sourceAsMap.getOrElse("authors", Seq()) match {
        case authors if authors == null => Seq()
        case authors => authors.asInstanceOf[List[String]]
      },
      hit.sourceAsMap.getOrElse("crawl_time", 0) match {
        case time: String => DateTime.parse(time).getMillis
        case _ => 0
      },
      hit.sourceAsMap.getOrElse("department", Seq()) match {
        case department if department == null => Seq()
        case department => department.asInstanceOf[List[String]]
      },
      hit.sourceAsMap.getOrElse("description", "") match {
        case description: String => description
        case _ => ""
      },
      hit.sourceAsMap.getOrElse("image_links", Seq()) match {
        case imageLinks if imageLinks == null => Seq()
        case imageLinks => imageLinks.asInstanceOf[List[String]]
      },
      hit.sourceAsMap.getOrElse("intro", "") match {
        case intro: String => intro
        case _ => ""
      },
      hit.sourceAsMap.getOrElse("keywords", Seq()) match {
        case keywords if keywords == null => Seq()
        case keywords => keywords.asInstanceOf[List[String]]
      },
      hit.sourceAsMap.getOrElse("lemmatizer", Seq()) match {
        case lemmas if lemmas == null => Seq()
        case lemmas => lemmas.asInstanceOf[List[String]]
      },
      hit.sourceAsMap.getOrElse("links", Seq()) match {
        case links if links == null => Seq()
        case links => links.asInstanceOf[List[String]]
      },
      hit.sourceAsMap.getOrElse("long_url", "") match {
        case longUrl: String => longUrl
        case _ => ""
      },
      hit.sourceAsMap.getOrElse("lemmatizer", Seq()) match {
        case mostRelevantLemmas if mostRelevantLemmas == null => Seq()
        case mostRelevantLemmas => mostRelevantLemmas.asInstanceOf[List[String]]
      },
      hit.sourceAsMap.getOrElse("news_site", "") match {
        case newsSite: String => newsSite
        case _ => ""
      },
      hit.sourceAsMap.getOrElse("published_time", 0) match {
        case time: String => DateTime.parse(time).getMillis
        case _ => 0
      },
      hit.sourceAsMap.getOrElse("read_time", 0.0) match {
        case readingTime: Double => readingTime
        case _ => 0.0
      },
      hit.sourceAsMap.getOrElse("text", "") match {
        case text: String => text
        case _ => ""
      },
      hit.sourceAsMap.getOrElse("textSum", "") match {
        case textSum: String => textSum
        case _ => ""
      },
      hit.sourceAsMap.getOrElse("title", "") match {
        case title: String => title
        case _ => ""
      },
        hit.sourceAsMap.getOrElse("entities", Seq()) match {
        case entities if entities == null => Seq()
        case entities => entities.asInstanceOf[List[String]]
      },
      hit.sourceAsMap.getOrElse("keywords_extracted", Seq()) match {
        case keywordsExtracted if keywordsExtracted == null => Seq()
        case keywordsExtracted => keywordsExtracted.asInstanceOf[List[String]]
      },
      hit.sourceAsMap.getOrElse("short_url", "") match {
        case shortUrl: String => shortUrl
        case _ => ""
      },
      hit.sourceAsMap.getOrElse("sentimens", 0.0) match {
        case sentiments: Double => sentiments
        case _ => 0.0
      }
    ))
  }
}
