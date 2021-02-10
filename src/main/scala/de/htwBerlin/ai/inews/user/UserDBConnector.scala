package de.htwBerlin.ai.inews.user

import com.typesafe.config.ConfigFactory
import de.htwBerlin.ai.inews.author.DB
import org.bson.codecs.MapCodec
import org.bson.codecs.configuration.CodecRegistries
import org.bson.codecs.configuration.CodecRegistries._
import org.mongodb.scala.MongoClient.DEFAULT_CODEC_REGISTRY
import org.bson.conversions.Bson
import org.json4s.DefaultFormats
import org.mongodb.scala.MongoClient.DEFAULT_CODEC_REGISTRY
import org.mongodb.scala.bson.{Document, ObjectId}
import org.mongodb.scala.model.Filters.equal

import scala.concurrent._
import ExecutionContext.Implicits.global
import scala.concurrent.duration.{Duration, DurationInt, SECONDS}
import scala.util.{Failure, Success}
object UserDBConnector {
  /**
   * Inserts user document to database.
   * @param document user
   * @return
   */
  def insertDocument(user: User): Unit = {
    Await.result(DB.users.insertOne(user).toFuture, 10 seconds)
  }

  /**
   * Finds a user by id.
   * @param id user id
   * @return user document on success
   */
  def findUserById(id: ObjectId): Seq[User] = {
    Await.result(DB.users.find(equal("_id", id)).toFuture, 10 seconds)
  }

  /**
   * Finds a user by username.
   * @param username username
   * @return user document on success
   */
  def findUserByUsername(username: String): Seq[User] = {
    Await.result(DB.users.find(equal("username", username)).toFuture, 10 seconds)
  }

  /**
   * Finds user by email.
   * @param email email
   * @return user document on success
   */
  def findUserByEmail(email: String): Seq[User] = {
    Await.result(DB.users.find(equal("email", email)).toFuture, 10 seconds)
  }

  /**
   * Updates user with given id.
   * @param modifier new user data
   * @param id user id
   */
  def updateDocument(modifier: User, id: ObjectId): Unit = {
    val user = findUserById(id).head
    Await.result(DB.users.findOneAndDelete(Document("_id" -> id)).toFuture, 10 seconds)
    insertDocument(User(_id=user._id, username=user.username,
      email=modifier.email, password=modifier.password, darkMode=modifier.darkMode,
      suggestions=modifier.suggestions, keywords=modifier.keywords))
  }

  /**
   * Deletes user with given id.
   * @param id user id
   */
  def deleteDocument(id: ObjectId): Unit = {
    Await.result(DB.users.findOneAndDelete(Document("_id" -> id)).toFuture, 10 seconds)
  }
}

object DB {
  import org.bson.codecs.configuration.CodecRegistries._
  import org.mongodb.scala.bson.codecs.Macros._
  import org.mongodb.scala.{MongoClient, MongoCollection, MongoDatabase}

  private val config = ConfigFactory.load
  private val db = config.getString("mongoDB.userDB")
  private val coll = config.getString("mongoDB.userCollection")
  private val customCodecs = fromProviders(
    classOf[User])
  private val javaCodecs = fromCodecs(
    new MapCodec())
  private val codecRegistry = fromRegistries(customCodecs, javaCodecs, DEFAULT_CODEC_REGISTRY)

  private val authorDatabase: MongoDatabase = MongoClient().getDatabase(db)
    .withCodecRegistry(codecRegistry)
  val users: MongoCollection[User] = authorDatabase.getCollection(coll)
}