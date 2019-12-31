package de.htwBerlin.ai.inews.data

import com.sksamuel.elastic4s.http.JavaClient
import com.sksamuel.elastic4s.requests.searches.SearchResponse
import com.sksamuel.elastic4s.{ElasticClient, ElasticProperties, Response}
import com.sksamuel.elastic4s.ElasticDsl._
import com.typesafe.config.ConfigFactory
import de.htwBerlin.ai.inews.core.{Article, ArticleList}

import scala.concurrent.{ExecutionContext, Future}

class ArticleService()(implicit executionContext: ExecutionContext) {
  // load config values
  private val config = ConfigFactory.load
  private final val host = config.getString("elasticSearch.host")
  private final val port = config.getInt("elasticSearch.port")
  private final val indexName = config.getString("elasticSearch.articleIndex")

  // setup ES client
  private val uri = s"http://${host}:${port}"
  private val client: ElasticClient = ElasticClient(JavaClient(ElasticProperties(uri)))

  // this is needed to serialize ES results into Articles
  implicit val hitReader: ArticleHitReader.type = ArticleHitReader

  def getById(id: String): Future[Article] = {
    client.execute {
      search(indexName)
        .matchQuery("mongo_id", id)
    }.map { resp: Response[SearchResponse] =>
      resp.result.to[Article]
    }.map(articles => if (articles.nonEmpty) articles.head else Article.empty)
  }

  def getWithQuery(query: ArticleQueryDTO): Future[ArticleList] = {
    var request = search(indexName)
        .sortByFieldDesc("published_time")
        .from(query.offset)
        .size(query.count)

    query.query match {
      case Some(q) => request = request.query(q)
      case None =>
    }

    query.department match {
      case Some(d) => request = request.matchQuery("newsSite", d)
      case None =>
    }

    query.author match {
      case Some(a) => request = request.matchQuery("author", a)
      case None =>
    }

    client.execute {
      request
    }.map { resp: Response[SearchResponse] =>
      resp.result.to[Article]
    }.map(articles => ArticleList(articles.size, articles))
  }

  def getNewspapers: Future[Seq[String]] = {
    client.execute {
      search(indexName)
        .size(0)
        .aggs {
          termsAgg("newspapers", "newsSite")
        }
    }.map { resp: Response[SearchResponse] =>
      resp.result.aggs.terms("newspapers").buckets.map(_.key)
    }
  }

  def getAuthors(query: Option[String]): Future[Seq[String]] = {
    // TODO query
    client.execute {
      search(indexName)
        .size(0)
        .aggs {
          termsAgg("authors", "author")
        }
    }.map { resp: Response[SearchResponse] =>
      resp.result.aggs.terms("authors").buckets.map(_.key)
    }
  }
}
