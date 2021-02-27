package de.htwBerlin.ai.inews.user

import com.typesafe.config.ConfigFactory
import org.bson.codecs.MapCodec
import org.mongodb.scala.MongoClient.DEFAULT_CODEC_REGISTRY
import org.mongodb.scala.bson.{Document, ObjectId}
import org.mongodb.scala.model.Filters.equal

import scala.concurrent._
import scala.concurrent.duration.DurationInt

/**
 * Connector for interactions with user collection in mongoDB.
 */
object UserDBConnector {
  /**
   * Finds a user by username.
   *
   * @param username username
   * @return user or none
   */
  def findUserByUsername(username: String): Option[User] = {
    Await.result(DB.users.find(equal("username", username)).toFuture, 10 seconds).headOption
  }

  /**
   * Finds user by email.
   *
   * @param email email
   * @return user or none
   */
  def findUserByEmail(email: String): Option[User] = {
    Await.result(DB.users.find(equal("email", email)).toFuture, 10 seconds).headOption
  }

  /**
   * Updates user with given id.
   *
   * @param modifier new user data
   * @param id       user id
   */
  def updateDocument(modifier: User, id: ObjectId): Unit = {
    val user = findUserById(id).head
    // dirty hack: keyword map does not allow to simply perform an update operation on user!
    Await.result(DB.users.findOneAndDelete(Document("_id" -> id)).toFuture, 10 seconds)
    insertDocument(User(_id = user._id, username = user.username,
      email = modifier.email, password = modifier.password, darkMode = modifier.darkMode,
      suggestions = modifier.suggestions, keywords = modifier.keywords))
  }

  /**
   * Inserts user document to database.
   *
   * @param user
   * @return
   */
  def insertDocument(user: User): Unit = {
    Await.result(DB.users.insertOne(user).toFuture, 10 seconds)
  }

  /**
   * Finds a user by id.
   *
   * @param id user id
   * @return user or none
   */
  def findUserById(id: ObjectId): Option[User] = {
    Await.result(DB.users.find(equal("_id", id)).toFuture, 10 seconds).headOption
  }

  /**
   * Deletes user with given id.
   *
   * @param id user id
   */
  def deleteDocument(id: ObjectId): Unit = {
    Await.result(DB.users.findOneAndDelete(Document("_id" -> id)).toFuture, 10 seconds)
  }
}

/**
 * Establishes connection with user collection in mongoDB.
 */
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
  private val userDatabase: MongoDatabase = MongoClient().getDatabase(db)
    .withCodecRegistry(codecRegistry)
  val users: MongoCollection[User] = userDatabase.getCollection(coll)
}