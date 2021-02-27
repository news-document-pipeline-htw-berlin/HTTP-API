package de.htwBerlin.ai.inews.user

import akka.http.scaladsl.model.headers.HttpCookie
import akka.http.scaladsl.model.{DateTime, StatusCodes}
import akka.http.scaladsl.server.{Directives, Route}
import de.htwBerlin.ai.inews.common.{Error, JWT}
import de.htwBerlin.ai.inews.core.Article.ArticleList
import de.htwBerlin.ai.inews.data.ArticleService
import org.bson.types.ObjectId
import org.mindrot.jbcrypt.BCrypt
import org.mongodb.scala.bson.BsonObjectId

import scala.concurrent.{ExecutionContext, Future}

/**
 * Service for handling user related requests.
 *
 * @param executionContext executionContext
 */
class UserService(articleService: ArticleService)(implicit executionContext: ExecutionContext) extends Directives with JsonSupport {

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
        val user = UserDBConnector.findUserByUsername(lr.user).get
        val expires = Option(if (lr.rememberMe) DateTime.MaxValue else null)
        val jwtToken = JWT.generateToken(
          user.username,
          user._id.toString,
          rememberMe = lr.rememberMe,
          darkMode = user.darkMode,
          suggestions = user.suggestions)
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
      case Error.OK =>
        createUser(sur)
        complete(StatusCodes.OK, "A new account for " + sur.username + " was created.")
      case _ =>
        Error.processError(e)
    }
  }

  /**
   * Creates a new user in database.
   *
   * @param sur signup data
   */
  private def createUser(sur: SignUpRequest): Unit = {
    val hashedPassword = BCrypt.hashpw(sur.password, BCrypt.gensalt())
    val user = User(
      ObjectId.get(),
      sur.username,
      sur.email,
      hashedPassword,
      suggestions = true,
      darkMode = false,
      Map.empty
    )
    UserDBConnector.insertDocument(user)
  }

  /**
   * Checks if signup data is valid.
   *
   * @param sur signup data
   * @return error value
   */
  private def validateSignUp(sur: SignUpRequest): Error.Value = {
    var user = UserDBConnector.findUserByUsername(sur.username)
    if (user.isDefined)
      return Error.USERNAME_TAKEN

    user = UserDBConnector.findUserByEmail(sur.email)
    if (user.isDefined)
      return Error.EMAIL_TAKEN

    if (sur.password != sur.password_rep)
      return Error.PASSWORD_MISMATCH

    Error.OK
  }

  /**
   * Handles a request to get user data.
   *
   * @param id id of user
   * @return user in body on success
   */
  def getUserData(id: String): Route = {
    val user = UserDBConnector.findUserById(BsonObjectId.apply(id).getValue)
    if (user.isEmpty)
      return complete(StatusCodes.NotFound, "User with id '" + id + "' could not be found.")

    val data = user.get
    complete(UserData(
      data._id.toString,
      data.username,
      data.email,
      data.password,
      data.suggestions,
      data.darkMode))
  }

  /**
   * Handles a request to update user data.
   *
   * @param ud user data
   * @return status code
   */
  def updateUserData(ud: UserData, id: String): Route = {
    if (!BsonObjectId.apply(id).getValue.equals(ud._id)) {
      complete(StatusCodes.Unauthorized, "Unauthorized to alter user data.")
    }

    val opt = UserDBConnector.findUserById(BsonObjectId.apply(ud._id).getValue)
    if (opt.isEmpty)
      complete(StatusCodes.NotFound, "User with id '" + ud._id + "' could not be found.")

    val user = opt.get
    updateUser(User(user._id, user.username, ud.email, user.password, ud.suggestions, ud.darkMode, user.keywords))
    complete(StatusCodes.OK, "User Data for '" + ud.username + "' has been updated.")
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
        val opt = UserDBConnector.findUserByUsername(cpr.user)
        if (opt.isEmpty)
          complete(StatusCodes.NotFound, "User '" + cpr.user + "' could not be found.")
        val user = opt.get
        updateUser(User(user._id, user.username, user.email, hashedPassword, user.suggestions, user.darkMode, user.keywords))
        complete(StatusCodes.OK, "Password for '" + cpr.user + "' has been changed.")
      }
      case _ =>
        Error.processError(e)
    }
  }

  /**
   * Checks if given username and password are valid.
   *
   * @param username username
   * @param password password
   * @return error value
   */
  private def validateCredentials(username: String, password: String): Error.Value = {
    val user = UserDBConnector.findUserByUsername(username)
    if (user.isEmpty)
      return Error.USER_NOT_FOUND
    if (!BCrypt.checkpw(password, user.head.password))
      return Error.INVALID_PASSWORD
    Error.OK
  }

  /**
   * Updates given user in database.
   *
   * @param user user
   */
  private def updateUser(user: User): Unit = {
    UserDBConnector.updateDocument(user, user._id)
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
        val opt = UserDBConnector.findUserByUsername(ar.user)
        if (opt.isEmpty)
          complete(StatusCodes.NotFound, "User '" + ar.user + "' could not be found.")
        val user = opt.get
        UserDBConnector.deleteDocument(user._id)
        complete(StatusCodes.NoContent, "User " + ar.user + " has been deleted.")
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
        val opt = UserDBConnector.findUserByUsername(ar.user)
        if (opt.isEmpty)
          complete(StatusCodes.NotFound, "User '" + ar.user + "' could not be found.")
        val user = opt.get
        updateUser(User(user._id, user.username, user.email, user.password, user.suggestions, user.darkMode, Map.empty))
        complete(StatusCodes.OK, "Account for " + ar.user + " and all its data has been deleted.")
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
  def getKeywordCount(id: String): Route = {
    complete(StatusCodes.OK, getKeywordsFromUser(id).size.toString)
  }

  /**
   * Updates keyword map for given user.
   *
   * @param userId   user
   * @param keyWords list of keywords
   * @return HTTP 204
   */
  def updateKeywords(userId: String, keyWords: KeyWords): Route = {
    val keyWordsList = keyWords.list.map(_.toLowerCase)
    val opt = UserDBConnector.findUserById(BsonObjectId.apply(userId).getValue)
    if (opt.isEmpty)
      return complete(StatusCodes.NotFound, "User " + userId + " not found.")

    val user = opt.get
    val newWords = collection.mutable.Map(user.keywords.toSeq: _*)
    (user.keywords.keys.toList ++ keyWords.list).map(_.toLowerCase).foreach(w => {
      if (keyWordsList.contains(w) && user.keywords.keySet.contains(w)) {
        newWords.update(w, user.keywords(w) + 1)
      } else if (user.keywords.keySet.contains(w)) {
        newWords.update(w, user.keywords(w))
      } else {
        newWords.update(w, 1)
      }
    })
    updateUser(User(user._id, user.username, user.email, user.password, user.suggestions, user.darkMode, newWords.toMap))
    complete(StatusCodes.NoContent)
  }

  /**
   * Retrieves article suggestions for given user.
   *
   * @param userID user
   * @param offset defined by page selection in frontend
   * @param count  amount of articles per page
   * @return list of articles
   */
  def getSuggestionsByKeywords(userID: String, offset: Int, count: Int): Future[ArticleList] = {
    val keywords = getKeywordsFromUser(userID)
    articleService.getArticlesByKeywords(keywords, offset, count)
  }

  /**
   * Retrieves the keywords for given user.
   *
   * @param userID user
   * @return map of keywords
   */
  def getKeywordsFromUser(userID: String): Map[String, Int] = {
    val user = UserDBConnector.findUserById(BsonObjectId.apply(userID).getValue)
    if (user.isDefined)
      return user.get.keywords
    Map.empty
  }
}
