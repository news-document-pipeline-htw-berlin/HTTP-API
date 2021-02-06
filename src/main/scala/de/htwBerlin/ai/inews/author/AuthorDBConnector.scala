package de.htwBerlin.ai.inews.author

import com.typesafe.config.ConfigFactory
import org.json4s.DefaultFormats
import reactivemongo.api.bson.collection.BSONCollection
import reactivemongo.api.{AsyncDriver, MongoConnection}
import reactivemongo.api.bson.{BSONDocument, BSONHandler, Macros}

import scala.concurrent._
import ExecutionContext.Implicits.global
import scala.concurrent.duration.{Duration, SECONDS}
object AuthorDBConnector {
  private val config = ConfigFactory.load
  private val db = config.getString("mongoDB.authorDB")
  private val coll = config.getString("mongoDB.authorCollection")

  val mongoUri = "mongodb://" + config.getString("mongoDB.host") + ":"+ config.getString("mongoDB.port") + "/" + db
  val driver = new AsyncDriver()

  val database = for {
    uri <- MongoConnection.fromString(mongoUri)
    con <- driver.connect(uri)
    dn <- Future(uri.db.get)
    db <- con.database(dn)
  } yield db

  implicit val formats: DefaultFormats = DefaultFormats
  implicit val authorHandler: BSONHandler[Author] = Macros.handler[Author]

  /**
   * Establishes db connection
   * @param connection
   * @return
   */
  def dbFromConnection(connection: MongoConnection): Future[BSONCollection] =
    connection.database(db).map(_.collection(coll))

  /**
   * Finds an author by id in database.
   * @param id author id
   * @return Author Document on success
   */
  def findAuthorById(id: String): Option[BSONDocument] = {
    val collection = dbFromConnection(Await.result(database.map(_.connection),
      Duration(1, SECONDS)))

    val query = BSONDocument("_id" -> id)
    val doc = Await.result(collection, Duration(1, SECONDS)).find(query).one[BSONDocument]
    Await.result(doc, Duration(1, SECONDS))
  }
}
