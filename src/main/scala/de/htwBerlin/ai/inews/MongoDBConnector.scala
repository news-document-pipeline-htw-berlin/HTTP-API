package de.htwBerlin.ai.inews

import reactivemongo.api.bson.collection.BSONCollection
import reactivemongo.api.{AsyncDriver, Cursor, DB, MongoConnection, bson}
import reactivemongo.api.bson.{BSONDocument, BSONDocumentReader, BSONDocumentWriter, Macros, document}
import reactivemongo.api.commands.WriteResult

import scala.concurrent._
import ExecutionContext.Implicits.global
import scala.util.{Failure, Success}

object MongoDBConnector {

  def dbFromConnection(connection: MongoConnection): Future[BSONCollection] =
    connection.database("userdb").map(_.collection("usercollection"))

  /*
  def authenticateDB(con: MongoConnection): Future[Unit] = {
    def username = "anyUser"
    def password = "correspondingPass"

    val futureAuthenticated = con.authenticate("mydb", username, password)

    futureAuthenticated.map { _ =>
      // doSomething
    }
  }
*/
  def insertDocument(collection: BSONCollection, document: BSONDocument): Future[Unit] = {
    val writer: Future[WriteResult] = collection.insert.one(document)

    writer.onComplete{
      case Failure(exception) => exception.printStackTrace()
      case Success(value) => //TODO: what to do on success
        println(s"inserterd document with result: $value")
    }
    writer.map(_ => {}) // do nothing with succes
  }

  /*def updateField(collection: BSONCollection, key: String, value: Any, id: Int): Future[Option[User]] = {
    val modifiedValue = key match {
      case "username" => value.asInstanceOf[String]
      case "email" => value.asInstanceOf[String]
      case "password" => value.asInstanceOf[String]
      case "suggestions" => value.asInstanceOf[Boolean]
      case "darkMode" => value.asInstanceOf[Boolean]

    }

    implicit val reader = Macros.reader[User]

    val result = collection.findAndUpdate(
      BSONDocument("id" -> id),
      BSONDocument("$set" -> BSONDocument(key -> modifiedValue)),
      fetchNewObject = true
    )
    result.map(_.result[User])
  }*/

  def updateDocument(collection: BSONCollection, modifier: BSONDocument, id: Int) = {
    val selector = BSONDocument("id" -> id)
    val futureUpdate = collection.update.one(
      q = selector, u = modifier, upsert = false, multi = false
    )
  }

  def deleteDocument(collection: BSONCollection, id: Int) = {
    val selector = BSONDocument("id" -> id)
    val futureRemove = collection.delete.one(selector)
    futureRemove.onComplete {
      case Failure(e) => throw e
      case Success(writeResult) => //TODO: display successfully deleted user
        println("successfully deleted ")
    }
  }
}
