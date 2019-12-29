package de.htwBerlin.ai.inews.data

import com.sksamuel.elastic4s.http.JavaClient
import com.sksamuel.elastic4s.requests.searches.SearchResponse
import com.sksamuel.elastic4s.{ElasticClient, ElasticProperties, Response}
import com.typesafe.config.ConfigFactory
import de.htwBerlin.ai.inews.core.Article

import scala.concurrent.{ExecutionContext, Future}

class ArticleService()(implicit executionContext: ExecutionContext) {
  private val config = ConfigFactory.load
  private final val host = config.getString("elasticSearch.host")
  private final val port = config.getInt("elasticSearch.port")
  private final val indexName = config.getString("elasticSearch.articleIndex")

  private val uri = s"http://${host}:${port}"
  private val client: ElasticClient = ElasticClient(JavaClient(ElasticProperties(uri)))

  implicit val hitReader: ArticleHitReader = new ArticleHitReader

  import com.sksamuel.elastic4s.ElasticDsl._

  // elastic search client
  def getById(id: String): Future[Article] = {
    client.execute {
      search(indexName)
        .matchQuery("mongo_id", id)
    }.map { resp: Response[SearchResponse] =>
      resp.result.to[Article]
    }.map(_.head)
  }

  def getWithQuery(query: ArticleQueryDTO): Future[Seq[Article]] = {
    client.execute {
      search(indexName)
        .sortByFieldDesc("published_time")
        .from(query.offset)
        .size(query.count)
    }.map { resp: Response[SearchResponse] =>
      resp.result.to[Article]
    }
  }

  def getNewspapers: Future[Seq[String]] = {
    client.execute {
      search(indexName)
        .size(0)
        .aggs {
          termsAgg("newspapers", "news_site")
        }
    }.map { resp: Response[SearchResponse] =>
      //resp.result.aggs.terms("newspapers").buckets.map(_.key)
      //Seq(resp.error.toString)
      Seq("")
    }
  }

  def getAuthors(query: Option[String]): Future[Seq[String]] = {
    client.execute {
      search(indexName)
        .size(0)
        .aggs {
          termsAgg("authors", "author")
        }
    }.map { resp: Response[SearchResponse] =>
      Seq("")
    }
  }
}
