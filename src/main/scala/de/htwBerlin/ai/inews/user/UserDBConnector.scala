package de.htwBerlin.ai.inews.user

import com.typesafe.config.ConfigFactory
import org.json4s.DefaultFormats
import reactivemongo.api.bson.collection.BSONCollection
import reactivemongo.api.{AsyncDriver, MongoConnection}
import reactivemongo.api.bson.{BSONDocument, BSONHandler, BSONObjectID, Macros}
import reactivemongo.api.commands.WriteResult

import scala.concurrent._
import ExecutionContext.Implicits.global
import scala.concurrent.duration.{Duration, SECONDS}
import scala.util.{Failure, Success}
object UserDBConnector {
  private val config = ConfigFactory.load
  private val db = config.getString("mongoDB.userDB")
  private val coll = config.getString("mongoDB.userCollection")

  val mongoUri = "mongodb://" + config.getString("mongoDB.host") + ":"+ config.getString("mongoDB.port") + "/" + db
  val driver = new AsyncDriver()

  val database = for {
    uri <- MongoConnection.fromString(mongoUri)
    con <- driver.connect(uri)
    dn <- Future(uri.db.get)
    db <- con.database(dn)
  } yield db

  implicit val formats: DefaultFormats = DefaultFormats
  implicit val userHandler: BSONHandler[User] = Macros.handler[User]

  /**
   * Establishes db connection
   * @param connection
   * @return
   */
  def dbFromConnection(connection: MongoConnection): Future[BSONCollection] =
    connection.database(db).map(_.collection(coll))

  /**
   * Inserts user document to database.
   * @param document user
   * @return
   */
  def insertDocument(document: BSONDocument): Future[Unit] = {
    val collection = dbFromConnection(Await.result(database.map(_.connection),
      Duration(1, SECONDS)))
    val writer: Future[WriteResult] = Await.result(collection, Duration(1, SECONDS)).insert.one(document)

    writer.onComplete{
      case Failure(exception) => exception.printStackTrace()
      case Success(value) => //TODO: what to do on success
        println(s"inserted document with result: $value")
    }
    writer.map(_ => {}) // do nothing with success
  }

  /**
   * Finds a user by id.
   * @param id user id
   * @return user document on success
   */
  def findUserById(id: BSONObjectID): Option[BSONDocument] = {
    val collection = dbFromConnection(Await.result(database.map(_.connection),
      Duration(1, SECONDS)))
    val query = BSONDocument("_id" -> id)
    val doc = Await.result(collection, Duration(1, SECONDS)).find(query).one[BSONDocument]
    Await.result(doc, Duration(1, SECONDS))
  }

  /**
   * Finds a user by username.
   * @param username username
   * @return user document on success
   */
  def findUserByUsername(username: String): Option[BSONDocument] = {
    val collection = dbFromConnection(Await.result(database.map(_.connection),
      Duration(1, SECONDS)))
    val query = BSONDocument("username" -> username)
    val doc = Await.result(collection, Duration(1, SECONDS)).find(query).one[BSONDocument]
    Await.result(doc, Duration(1, SECONDS))
  }

  /**
   * Finds user by email.
   * @param email email
   * @return user document on success
   */
  def findUserByEmail(email: String): Option[BSONDocument] = {
    val collection = dbFromConnection(Await.result(database.map(_.connection),
      Duration(1, SECONDS)))
    val query = BSONDocument("email" -> email)
    val doc = Await.result(collection, Duration(1, SECONDS)).find(query).one[BSONDocument]
    Await.result(doc, Duration(1, SECONDS))
  }

  /**
   * Updates user with given id.
   * @param modifier new user data
   * @param id user id
   */
  def updateDocument(modifier: BSONDocument, id: BSONObjectID): Unit = {
    val collection = dbFromConnection(Await.result(database.map(_.connection),
      Duration(1, SECONDS)))
    val selector = BSONDocument("_id" -> id)
    val futureUpdate = Await.result(collection, Duration(1, SECONDS)).update.one(
      q = selector, u = modifier, upsert = false, multi = false
    )
  }

  /**
   * Deletes user with given id.
   * @param id user id
   */
  def deleteDocument(id: BSONObjectID): Unit = {
    val collection = dbFromConnection(Await.result(database.map(_.connection),
      Duration(1, SECONDS)))
    val selector = BSONDocument("_id" -> id)
    val futureRemove = Await.result(collection, Duration(1, SECONDS)).delete.one(selector)
    futureRemove.onComplete {
      case Failure(e) => throw e
      case Success(writeResult) => //TODO: display successfully deleted user
        println("successfully deleted ")
    }
  }
}
