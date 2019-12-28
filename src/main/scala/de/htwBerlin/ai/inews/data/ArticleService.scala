package de.htwBerlin.ai.inews.data

import akka.actor.ActorSystem
import akka.stream.SystemMaterializer
import akka.stream.alpakka.elasticsearch.scaladsl.ElasticsearchSource
import akka.stream.alpakka.elasticsearch.{ElasticsearchSourceSettings, ReadResult}
import akka.stream.scaladsl.Sink
import com.typesafe.config.ConfigFactory
import de.htwBerlin.ai.inews.core.Article
import org.apache.http.HttpHost
import org.elasticsearch.client.RestClient

import scala.concurrent.Future

object ArticleService {
  private val config = ConfigFactory.load

  // The system coordinates actors and provides threads for them
  implicit val actorSystem: ActorSystem = ActorSystem()
  // The materializer makes actors execute graphs
  implicit val materializer: SystemMaterializer.type = SystemMaterializer

  private final val host = config.getString("elasticSearch.host")
  private final val port = config.getInt("elasticSearch.port")
  private final val indexName = config.getString("elasticSearch.articleIndex")

  // elastic search client
  implicit val client: RestClient = RestClient.builder(
    new HttpHost(host, port)
  ).build()

  def getById(id: String): Future[Article] = {
    ElasticsearchSource
      .typed[Article](
        indexName,
        Some("_doc"),
        searchParams = Map(
          "query" -> s""" {"match": { "mongo_id": "${id}"} } """,
        ),
        ElasticsearchSourceSettings()
      ).map { message: ReadResult[Article] =>
        message.source
      }
      .runWith(Sink.head[Article])
  }

  def getWithQuery(query: ArticleQueryDTO): Future[Seq[Article]] = {
    ElasticsearchSource
      .typed[Article](
        indexName,
        Some("_doc"),
        searchParams = Map(
          "query" -> s""" { "match_all": {} } """,
          "sort" -> s""" [ {"crawl_time": "desc"}, {"published_time": "desc"} ] """
        ),
        ElasticsearchSourceSettings()
      ).map { message: ReadResult[Article] =>
        message.source
      }
      .drop(query.offset)
      .take(query.count)
      .runWith(Sink.seq[Article])
  }

  def getNewspapers: Future[Set[String]] = {
    /*ElasticsearchSource
      .typed[Article](
        indexName,
        Some("_doc"),
        searchParams = Map(
          "query" -> s""" { "match_all": {} } """,
        ),
        ElasticsearchSourceSettings()
      ).map { message: ReadResult[Article] =>
      message.source
    }
    .runWith(Sink.fold(Set[String]()) ((set, article) => set + article.news_site))*/ // get unique values
    ???
  }

  def getAuthors(query: Option[String]): Future[Seq[String]] = {
    ???
  }
}
