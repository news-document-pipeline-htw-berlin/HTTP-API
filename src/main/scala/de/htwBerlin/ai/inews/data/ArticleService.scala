package de.htwBerlin.ai.inews.data

import com.sksamuel.elastic4s.http.JavaClient
import com.sksamuel.elastic4s.requests.searches.{DateHistogramInterval, SearchResponse}
import com.sksamuel.elastic4s.{ElasticClient, ElasticDate, ElasticProperties, Response}
import com.sksamuel.elastic4s.ElasticDsl._
import com.typesafe.config.ConfigFactory
import de.htwBerlin.ai.inews.core.Analytics.{Analytics, TermNotFoundException, TermOccurrence}
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
        .matchQuery("_id", id)
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
      case Some(q) if !q.isEmpty => request = request.query(q)
      case _ =>
    }

    if (query.departments.exists(!_.isEmpty)) {
      request = request.bool(
        should(
          {
            for (department <- query.departments.filter(!_.isEmpty)) yield {
              matchQuery("departments", department)
            }
          }
        )
      )
    }

    if (query.newspapers.exists(!_.isEmpty)) {
      request = request.bool(
        should(
          {
            for (newspaper <- query.newspapers.filter(!_.isEmpty)) yield {
              matchQuery("newsSite", newspaper)
            }
          }
        )
      )
    }

    query.author match {
      case Some(a) if !a.isEmpty => request = request.matchQuery("authors", a)
      case _ =>
    }

    client.execute {
      request
    }.map { resp: Response[SearchResponse] =>
      ArticleList(resp.result.totalHits, resp.result.to[Article])
    }
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
    client.execute {
      search(indexName)
        .size(0)
        .aggs {
          termsAgg("authors", "authors")
        }
    }.map { resp: Response[SearchResponse] =>
      val res = resp.result.aggs.terms("authors").buckets.map(_.key)

      query match {
        case Some(author) => res.filter(_.toLowerCase.contains(author.toLowerCase))
        case _ => res
      }
    }
  }

  def getAnalytics(q: String, timeFrom: Long, timeTo: Long): Future[Analytics] = {
    client.execute {
      search(indexName)
        .bool(
          must(
            multiMatchQuery(q)
              .fields("title", "description", "intro", "text"),
            rangeQuery("publishedTime")
              .gt(ElasticDate.fromTimestamp(timeFrom))
              .lt(ElasticDate.fromTimestamp(timeTo))
          )
        )
        .aggs {
          dateHistogramAggregation("word_occurrence_over_time")
            .field("publishedTime")
            .calendarInterval(DateHistogramInterval.Day)
        }
    }.map { resp: Response[SearchResponse] =>
      if (resp.result.size == 0)
        throw TermNotFoundException("Term not found! ")

      val occurrences = resp.result.aggs.dateHistogram("word_occurrence_over_time")
        .buckets.map(bucket => {
        val data = bucket.dataAsMap
        TermOccurrence(
          data.getOrElse("key", 0).asInstanceOf[Long],
          data.getOrElse("key_as_string", "").toString,
          data.getOrElse("doc_count", 0).asInstanceOf[Int])
      })

      Analytics(resp.result.totalHits, q, timeFrom, timeTo, occurrences.filter(_.occurrences > 0))
    }
  }
}
