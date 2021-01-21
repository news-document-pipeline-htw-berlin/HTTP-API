package de.htwBerlin.ai.inews.user

import reactivemongo.api.bson.{BSONDocument, BSONDocumentReader, BSONDocumentWriter, BSONObjectID}

import scala.collection.mutable
import scala.collection.immutable
import scala.util.{Success, Try}


case class User(id: BSONObjectID, username: String, email: String, password: String, suggestions: Boolean, darkMode: Boolean, var keywords: immutable.Map[String, Int])

object User {

  implicit object UserReader extends BSONDocumentReader[User] {
    override def readDocument(doc: BSONDocument): Try[User] = for {
      id <- doc.getAsTry[BSONObjectID]("_id")
      username <- doc.getAsTry[String]("username")
      email <- doc.getAsTry[String]("email")
      password <- doc.getAsTry[String]("password")
      suggestions <- doc.getAsTry[Boolean]("suggestions")
      darkMode <- doc.getAsTry[Boolean]("darkMode")
      keywords <- doc.getAsTry[immutable.Map[String, Int]]("keywords")
    } yield User(id, username, email, password, suggestions, darkMode, keywords)
  }

  implicit object UserWriter extends BSONDocumentWriter[User] {
    override def writeTry(user: User): Try[BSONDocument] = {
      Success(
        BSONDocument(
          "_id" -> user.id,
          "username" -> user.username,
          "email" -> user.email,
          "password" -> user.password,
          "suggestions" -> user.suggestions,
          "darkMode" -> user.darkMode,
          "keywords" -> user.keywords
        )
      )
    }
  }
}
