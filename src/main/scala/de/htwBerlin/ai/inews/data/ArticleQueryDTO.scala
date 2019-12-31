package de.htwBerlin.ai.inews.data

final case class ArticleQueryDTO (
  offset: Int,
  count: Int,
  query: Option[String],
  department: Option[String],
  author: Option[String]
)
