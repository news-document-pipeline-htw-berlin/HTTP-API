package de.htwBerlin.ai.inews.user

import reactivemongo.api.bson.{BSONDocument, BSONDocumentReader, BSONObjectID}

import scala.util.Try

case class User(id: BSONObjectID, username: String, email: String, password: String, suggestions: Boolean, darkMode: Boolean)

object User {
  implicit object UserReader extends BSONDocumentReader[User] {
    override def readDocument(doc: BSONDocument): Try[User] = for {
      id <- doc.getAsTry[BSONObjectID]("_id")
      username <- doc.getAsTry[String]("username")
      email <- doc.getAsTry[String]("email")
      password <- doc.getAsTry[String]("password")
      suggestions <- doc.getAsTry[Boolean]("suggestions")
      darkMode <- doc.getAsTry[Boolean]("darkMode")
    } yield User(id, username, email, password, suggestions, darkMode)
  }
}
