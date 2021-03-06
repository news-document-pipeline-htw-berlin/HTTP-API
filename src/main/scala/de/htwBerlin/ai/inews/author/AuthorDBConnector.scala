package de.htwBerlin.ai.inews.author

import com.typesafe.config.ConfigFactory
import org.mongodb.scala.MongoClient.DEFAULT_CODEC_REGISTRY
import org.mongodb.scala.model.Filters.equal
import scala.concurrent.Await
import scala.concurrent.duration._
import scala.language.postfixOps

/**
 * Connector for interactions with author collection in mongoDB.
 */
object AuthorDBConnector {
  /**
   * Finds author with given id in database.
   * @param id author name
   * @return author or none
   */
  def findAuthorById(id: String): Option[Author] = {
    Await.result(DB.authors.find(equal("_id", id)).toFuture, 10 seconds).headOption
  }
}

/**
 * Establishes connection with author collection in mongoDB.
 */
object DB {
  import org.bson.codecs.configuration.CodecRegistries._
  import org.mongodb.scala.bson.codecs.Macros._
  import org.mongodb.scala.{MongoClient, MongoCollection, MongoDatabase}

  private val config = ConfigFactory.load
  private val db = config.getString("mongoDB.authorDB")
  private val coll = config.getString("mongoDB.authorCollection")
  private val customCodecs = fromProviders(
    classOf[Author],
    classOf[AuthorIntMap],
    classOf[AuthorDoubleMap])
  private val codecRegistry = fromRegistries(customCodecs,
    DEFAULT_CODEC_REGISTRY)

  private val authorDatabase: MongoDatabase = MongoClient().getDatabase(db)
    .withCodecRegistry(codecRegistry)
  val authors: MongoCollection[Author] = authorDatabase.getCollection(coll)
}

