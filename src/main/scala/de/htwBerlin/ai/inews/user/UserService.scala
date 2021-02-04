package de.htwBerlin.ai.inews.user

import java.util.concurrent.TimeUnit
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.{DateTime, StatusCodes}
import akka.http.scaladsl.model.StatusCodes.NotFound
import akka.http.scaladsl.model.headers.HttpCookie
import akka.http.scaladsl.server.{Directives, Route}
import authentikat.jwt.{JsonWebToken, JwtClaimsSet, JwtHeader}
import de.htwBerlin.ai.inews.MongoDBConnector
import de.htwBerlin.ai.inews.user.User.UserWriter
import de.htwBerlin.ai.inews.common.{Error, JWT}
import de.htwBerlin.ai.inews.core.Article.ArticleList
import de.htwBerlin.ai.inews.data.ArticleService
import org.mindrot.jbcrypt.BCrypt
import reactivemongo.api.{AsyncDriver, DB, MongoConnection}
import spray.json.{DefaultJsonProtocol, RootJsonFormat}
import org.json4s.DefaultFormats
import org.json4s.native.Serialization.write
import reactivemongo.api.bson.collection.BSONCollection
import reactivemongo.api.bson.{BSONDocument, BSONDocumentReader, BSONDocumentWriter, BSONHandler, BSONObjectID, Macros, `null`}
import reactivemongo.core.nodeset.Authenticate
import de.htwBerlin.ai.inews.core.Article.JsonFormat._

import scala.concurrent.duration.{Duration, SECONDS}
import scala.concurrent.{Await, ExecutionContext, Future}

/**
 * Service for handling user related requests
 * @param executionContext executionContext
 */
class UserService(articleService: ArticleService)(implicit executionContext: ExecutionContext) extends Directives with JsonSupport {
  val mongoUri = "mongodb://127.0.0.1:27017/userdb" //?authMode=scram-sha1"
  val driver = new AsyncDriver()
  /*
  val mongoUserName = "userdbAdmin"
  val mongoPassword = "admin"
  val dbCredentials = List(Authenticate("userdb", mongoUserName, Some(mongoPassword)))
  */

  val database: Future[DB] = for {
    uri <- MongoConnection.fromString(mongoUri)
    con <- driver.connect(uri)
    dn <- Future(uri.db.get)
    db <- con.database(dn)
  } yield db
  implicit val formats: DefaultFormats = DefaultFormats
  implicit val userHandler: BSONHandler[User] = Macros.handler[User]

  /**
   * Extracts the ID string from a BSONObjectID
   *
   * @param id BSONObjectID
   * @return string of ID
   */
  private def convertId(id: BSONObjectID): String = {
    val str = id.toString()
    str.substring(str.indexOf('(') + 1, str.indexOf(')'))
  }

  /**
   * Creates a new user in database
   *
   * @param sur signup data
   */
  private def createUser(sur: SignUpRequest) = {
    val hashedPassword = BCrypt.hashpw(sur.password, BCrypt.gensalt())
    val bDoc = BSONDocument(
      "username" -> sur.username,
      "email" -> sur.email,
      "password" -> hashedPassword,
      "suggestions" -> true,
      "darkMode" -> false,
      "keywords" -> Map.empty[String, Int]
    )
    val collection = MongoDBConnector.dbFromConnection(Await.result(database.map(_.connection),
      Duration(1, SECONDS)))
    MongoDBConnector.insertDocument(Await.result(collection, Duration(1, SECONDS)), bDoc)
  }

  /**
   * Updates given user in database.
   *
   * @param user user
   */
  private def updateUser(user: User): Unit = {
    val collection = MongoDBConnector.dbFromConnection(Await.result(database.map(_.connection),
      Duration(1, SECONDS)))
    MongoDBConnector.updateDocument(Await.result(collection, Duration(1, SECONDS)),
      UserWriter.writeTry(user).get, user.id)
  }

  /**
   * Deletes given user in database.
   *
   * @param id user's id
   */
  private def deleteUser(id: BSONObjectID): Unit = {
    val collection = MongoDBConnector.dbFromConnection(Await.result(database.map(_.connection),
      Duration(1, SECONDS)))
    MongoDBConnector.deleteDocument(Await.result(collection, Duration(1, SECONDS)), id)
  }

  /**
   * Finds a user by username and returns the corresponding object
   *
   * @param username username
   * @return user object
   */
  private def getUserObjectByUsername(username: String): User = {
    val collection = MongoDBConnector.dbFromConnection(Await.result(database.map(_.connection),
      Duration(1, SECONDS)))
    val userDoc = MongoDBConnector.findUserByUsername(Await.result(collection, Duration(1, SECONDS)), username).get
    User.UserReader.readDocument(userDoc).get
  }

  /**
   * Finds a user by email and returns the corresponding object
   *
   * @param email email
   * @return user object
   */
  private def getUserObjectByEmail(email: String): User = {
    val collection = MongoDBConnector.dbFromConnection(Await.result(database.map(_.connection),
      Duration(1, SECONDS)))
    val userDoc = MongoDBConnector.findUserByEmail(Await.result(collection, Duration(1, SECONDS)), email).get
    User.UserReader.readDocument(userDoc).get
  }

  /**
   * Finds a user by id and returns the corresponding object
   *
   * @param id id
   * @return user object
   */
  private def getUserObjectById(id: String): User = {
    val collection = MongoDBConnector.dbFromConnection(Await.result(database.map(_.connection),
      Duration(1, SECONDS)))
    val userDoc = MongoDBConnector.findUserById(Await.result(collection, Duration(1, SECONDS)),
      BSONObjectID.parse(id).get).get
    User.UserReader.readDocument(userDoc).get
  }

  /**
   * Checks if given username and password are valid.
   *
   * @param username username
   * @param password password
   * @return error value
   */
  private def validateCredentials(username: String, password: String): Error.Value = {
    try {
      val user = getUserObjectByUsername(username)
      if (!BCrypt.checkpw(password, user.password))
        return Error.INVALID_PASSWORD
    } catch {
      case _: NoSuchElementException =>
        return Error.USER_NOT_FOUND
    }
    Error.OK
  }

  /**
   * Checks if signup data is valid.
   *
   * @param sur signup data
   * @return error value
   */
  private def validateSignUp(sur: SignUpRequest): Error.Value = {
    try {
      getUserObjectByUsername(sur.username)
      return Error.USERNAME_TAKEN
    } catch {
      case _: NoSuchElementException =>
        Error.OK
    }
    try {
      getUserObjectByEmail(sur.email)
      return Error.EMAIL_TAKEN
    } catch {
      case _: NoSuchElementException =>
        Error.OK
    }
    if (sur.password != sur.password_rep)
      return Error.PASSWORD_MISMATCH
    Error.OK
  }

  /**
   * Handles a login request.
   *
   * @param lr login request
   * @return set JWT cookie on success
   */
  def handleLogin(lr: LoginRequest): Route = {
    val e = validateCredentials(lr.user, lr.password)
    e match {
      case Error.OK =>
        val user = getUserObjectByUsername(lr.user)
        val expires = Option(if (lr.rememberMe) DateTime.MaxValue else null)
        val jwtToken = JWT.generateToken(user.username, convertId(user.id), rememberMe = lr.rememberMe,
          darkMode = user.darkMode, suggestions = user.suggestions)
        setCookie(HttpCookie("accessToken", value = jwtToken, path = Option("/"), expires = expires)) {
          complete(StatusCodes.OK, "Logged in successfully.")
        }
      case _ => Error.processError(e)
    }
  }

  /**
   * Handles a signup request.
   *
   * @param sur signup request
   * @return status code
   */
  def handleSignUp(sur: SignUpRequest): Route = {
    val e = validateSignUp(sur)
    e match {
      case Error.OK => {
        createUser(sur)
        complete(StatusCodes.OK, "A new account for " + sur.username + " was created.")
      }
      case _ =>
        Error.processError(e)
    }
  }

  /**
   * Handles a request to get user data.
   *
   * @param id id of user
   * @return user in body on success
   */
  def getUserData(id: String): Route = {
    try {
      val user = getUserObjectById(id)
      complete(BSONDocument.pretty(UserWriter.writeTry(user).get))
    } catch {
      case e: NoSuchElementException =>
        complete(StatusCodes.NotFound, "User with id '" + id + "' could not be found.")
    }
  }

  /**
   * Handles a request to update user data.
   *
   * @param ud user data
   * @return status code
   */
  def updateUserData(ud: UserData, id: String): Route = {
    if (!id.equals(ud._id)) {
      complete(StatusCodes.Unauthorized, "Unauthorized to alter user data.")
    }
    try {
      val user = getUserObjectById(ud._id)
      updateUser(User(user.id, user.username, ud.email, user.password, ud.suggestions, ud.darkMode, ud.keywords))
      complete(StatusCodes.OK, "User Data for '" + ud.username + "' has been updated.")
    } catch {
      case e: NoSuchElementException =>
        complete(StatusCodes.NotFound, "User with id '" + ud._id + "' could not be found.")
    }
  }

  /**
   * Handles a request to update a user's password.
   *
   * @param cpr change password request
   * @return status code
   */
  def updatePassword(cpr: ChangePasswordRequest): Route = {
    val e = validateCredentials(cpr.user, cpr.oldPW)
    e match {
      case Error.OK => {
        if (!cpr.newPW.equals(cpr.repPW)) {
          return complete(StatusCodes.BadRequest, "Password mismatch.")
        }
        val hashedPassword = BCrypt.hashpw(cpr.newPW, BCrypt.gensalt())
        try {
          val user = getUserObjectByUsername(cpr.user)
          updateUser(User(user.id, user.username, user.email, hashedPassword, user.suggestions, user.darkMode, user.keywords))
          complete(StatusCodes.OK, "Password for '" + cpr.user + "' has been changed.")
        } catch {
          case e: NoSuchElementException =>
            complete(StatusCodes.NotFound, "User '" + cpr.user + "' could not be found.")
        }
      }
      case _ =>
        Error.processError(e)
    }
  }

  /**
   * Checks credentials of login request.
   *
   * @param lr login request
   * @return status code
   */
  def isAuth(lr: LoginRequest): Route = {
    Error.processError(validateCredentials(lr.user, lr.password))
  }

  /**
   * Handles a delete user request.
   *
   * @param ar auth data
   * @return status code
   */
  def deleteUser(ar: AuthRequest): Route = {
    val e = validateCredentials(ar.user, ar.password)
    e match {
      case Error.OK =>
        try {
          val user = getUserObjectByUsername(ar.user)
          updateUser(user)
          complete(StatusCodes.OK, "Data of " + ar.user + " has been deleted.")
        } catch {
          case e: NoSuchElementException =>
            complete(StatusCodes.NotFound, "User '" + ar.user + "' could not be found.")
        }
      case _ =>
        Error.processError(e)
    }
  }

  /**
   * Handles a delete data request.
   *
   * @param ar auth data
   * @return status code
   */
  def deleteData(ar: AuthRequest): Route = {
    val e = validateCredentials(ar.user, ar.password)
    e match {
      case Error.OK =>
        try {
          val user = getUserObjectByUsername(ar.user)
          user.keywords = Map.empty
          updateUser(user)
          complete(StatusCodes.OK, "Account for " + ar.user + " and all its data has been deleted.")
        } catch {
          case e: NoSuchElementException =>
            complete(StatusCodes.NotFound, "User '" + ar.user + "' could not be found.")
        }
      case _ =>
        Error.processError(e)
    }
  }

  /**
   * Handles a get keywords request.
   *
   * @param id id of requesting user
   * @return count of keywords
   */
  def getKeywords(id: String): Route = {
    complete(StatusCodes.OK, getKeywordsFromUser(id).size.toString)
  }

  def updateKeywords(userId: String, keyWords: KeyWords): Route = {
    //TODO: Schön machen
    val keyWordsList = keyWords.list.map(_.toLowerCase)
    val user = getUserObjectById(userId)
    //val newWords = scala.collection.mutable.Map.empty[String, Int]
    val newWords = collection.mutable.Map(user.keywords.toSeq: _*)
    (user.keywords.keys.toList ++ keyWords.list).map(_.toLowerCase).foreach(w => {
      //val value = if(keyWords.list.contains(w)) 1 else 0
      if (keyWordsList.contains(w) && user.keywords.keySet.contains(w)) {
        newWords.update(w, user.keywords(w) + 1)
      } else if (user.keywords.keySet.contains(w)) {
        newWords.update(w, user.keywords(w))
      } else {
        newWords.update(w, 1)
      }
    })

    user.keywords = newWords.toMap
    updateUser(user)
    complete(StatusCodes.NoContent)
  }

  def getKeywordsFromUser(userID: String): Map[String, Int] = {
    val user = getUserObjectById(userID)
    user.keywords
  }

  def getSuggestionsByKeywords(userID: String, offset: Int, count: Int): Future[ArticleList] = {
    val keywords = getKeywordsFromUser(userID)
    articleService.getArticlesByKeywords(keywords, offset, count)
  }
}
