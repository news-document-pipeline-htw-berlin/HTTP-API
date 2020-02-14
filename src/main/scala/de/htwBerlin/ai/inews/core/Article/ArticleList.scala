package de.htwBerlin.ai.inews.core.Article

case class ArticleList(
  resultCount: Long,
  articles: Seq[Article]
)
