package de.htwBerlin.ai.inews.core

final case class ArticleQueryDTO (
  offset: Int,
  count: Int,
  query: String,
  department: String,
  author: String
)
