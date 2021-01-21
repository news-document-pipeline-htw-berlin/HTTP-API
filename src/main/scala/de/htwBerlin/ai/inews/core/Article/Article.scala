package de.htwBerlin.ai.inews.core.Article

case class Article(
  id: String,
  authors: Seq[String],
  crawlTime: Long,
  departments: Seq[String],
  description: String,
  imageLinks: Seq[String],
  intro: String,
  keywords: Seq[String],
  lemmas: Seq[String],
  links: Seq[String],
  longUrl: String,
  mostRelevantLemmas: Seq[String],
  newsSite: String,
  publishedTime: Long,
  readingTime: Double,
  text: String,
  title: String,
  entities: Seq[String],
  keywordsExtracted: Seq[String],
  shortUrl: String,
  sentiments: Double
)

object Article {
  def empty: Article = {
    new Article(
      "",
      Seq(),
      0L,
      Seq(),
      "",
      Seq(),
      "",
      Seq(),
      Seq(),
      Seq(),
      "",
      Seq(),
      "",
      0L,
      0,
      "",
      "",
      Seq(),
      Seq(),
      "",
      0.0
    )
  }
}
