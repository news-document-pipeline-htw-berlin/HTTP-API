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
  readingTime: Int,
  text: String,
  title: String
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
      ""
    )
  }
}
