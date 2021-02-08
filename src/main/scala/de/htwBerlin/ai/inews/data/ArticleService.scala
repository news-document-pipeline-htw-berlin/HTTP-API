package de.htwBerlin.ai.inews.data

import com.sksamuel.elastic4s.{Days, ElasticClient, ElasticDate, ElasticProperties, Response, Years}
import com.sksamuel.elastic4s.http.JavaClient
import com.sksamuel.elastic4s.requests.searches.{DateHistogramInterval, SearchResponse}
import com.sksamuel.elastic4s.ElasticDsl._
import com.sksamuel.elastic4s.requests.searches.queries.compound.BoolQueryBuilderFn
import com.sksamuel.elastic4s.requests.searches.queries.matches.MultiMatchQuery
import com.typesafe.config.ConfigFactory
import de.htwBerlin.ai.inews.core.Analytics.MostRelevantLemmas._
import de.htwBerlin.ai.inews.core.Analytics.TermOccurrence._
import de.htwBerlin.ai.inews.core.Article._

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

  final case class KeywordsQuery(
                                  keywords: Iterable[String]
                                )

  def getById(id: String): Future[Article] = {
    client.execute {
      search(indexName)
        .matchQuery("_id", id)
    }.map { resp: Response[SearchResponse] =>
      resp.result.to[Article]
    }.map(articles => if (articles.nonEmpty) articles.head else Article.empty)
  }

  def getArticlesByKeywords(keywords: Map[String, Int], offset: Int, count: Int): Future[ArticleList] = {
    val sortedKeywords = keywords.map(x => (x._2, x._1)).toList.sortBy(x => x._1).reverse
    client.execute {
      search(indexName)
        .bool(
          should(
            //(for (x <- sortedKeywords) yield termsQuery("keywords", x._2).boost(x._1)).take(5)
            (for (x <- sortedKeywords) yield multiMatchQuery(x._2).fields("keywords", "keywords_extracted").boost(x._1)).take(10)
          )
        )
        .sortByFieldDesc("published_time")
        .from(offset)
        .size(count)
    }
      .map { resp: Response[SearchResponse] =>
        ArticleList(resp.result.totalHits, resp.result.to[Article])
      }
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
              matchQuery("department", department)
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
              matchQuery("news_site", newspaper)
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

  def getDepartments: Future[Seq[String]] = {
    client.execute {
      search(indexName)
        .size(0)
        .aggs {
          termsAgg("department", "department.keyword")
            .size(100)
        }
    }.map { resp: Response[SearchResponse] =>
      resp.result.aggs.terms("department").buckets.map(_.key)
    }
  }

  def getNewspapers: Future[Seq[String]] = {
    client.execute {
      search(indexName)
        .size(0)
        .aggs {
          termsAgg("news_site", "news_site.keyword")
            .size(100) // optimistic
        }
    }.map { resp: Response[SearchResponse] =>
      resp.result.aggs.terms("news_site").buckets.map(_.key)
    }
  }

  def getAuthors(query: Option[String]): Future[Seq[String]] = {
    client.execute {
      search(indexName)
        .size(0)
        .aggs {
          termsAgg("authors", "authors.keyword")
            .size(10000)
        }
    }.map { resp: Response[SearchResponse] =>
      val res = resp.result.aggs.terms("authors").buckets.map(_.key)

      query match {
        case Some(author) => res.filter(_.toLowerCase.contains(author.toLowerCase))
        case _ => res
      }
    }
  }

  def getTermOccurrences(q: String, timeFrom: Long, timeTo: Long): Future[TermOccurrences] = {
    client.execute {
      search(indexName)
        .bool(
          must(
            multiMatchQuery(q)
              .fields("title", "description", "intro", "text"),
            rangeQuery("published_time")
              .gt(ElasticDate.fromTimestamp(timeFrom))
              .lt(ElasticDate.fromTimestamp(timeTo))
          )
        )
        .aggs {
          dateHistogramAggregation("termOccurrence")
            .field("published_time")
            .calendarInterval(DateHistogramInterval.Day)
            .minDocCount(0)
            .missing(0)
            .extendedBounds(ElasticDate.fromTimestamp(timeFrom), ElasticDate.fromTimestamp(timeTo))
        }
    }.map { resp: Response[SearchResponse] =>
      if (resp.result.size == 0)
        throw TermNotFoundException("Term not found! ")

      val occurrences = resp.result.aggregations.dateHistogram("termOccurrence")
        .buckets.map(bucket => {
        val data = bucket.dataAsMap
        TermOccurrence(
          data.getOrElse("key", 0).asInstanceOf[Long],
          data.getOrElse("key_as_string", "").toString,
          data.getOrElse("doc_count", 0).asInstanceOf[Int])
      })

      TermOccurrences(resp.result.totalHits, q, timeFrom, timeTo, occurrences)
    }
  }

  def getMostRelevantLemmas: Future[Lemmas] = {
    client.execute {
      search(indexName)
        .bool(
          must(
            rangeQuery("published_time")
              .gt(ElasticDate.now.minus(7, Days))
          )
        )
        .aggs(
          termsAgg("lemmatizer", "lemmatizer.keyword")
            .size(10)
        )
    }.map { resp: Response[SearchResponse] =>
      if (resp.result.size == 0)
        throw LemmasNotFoundException("No articles found. ")

      val lemmas = resp.result.aggregations.terms("lemmatizer")
        .buckets.map(bucket => Lemma(bucket.key, bucket.docCount))

      Lemmas(lemmas)
    }
  }
}
