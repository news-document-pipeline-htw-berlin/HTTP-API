package de.htwBerlin.ai.inews.data

final case class ArticleQueryDTO (
  offset: Int,
  count: Int,
  query: Option[String],
  departments: Iterable[String],
  newspapers: Iterable[String],
  author: Option[String]
)
