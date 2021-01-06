package de.htwBerlin.ai.inews

import reactivemongo.api.bson.collection.BSONCollection
import reactivemongo.api.{AsyncDriver, Cursor, DB, MongoConnection, bson}
import reactivemongo.api.bson.{BSONDocument, BSONDocumentReader, BSONDocumentWriter, BSONObjectID, Macros, document}
import reactivemongo.api.commands.WriteResult

import scala.concurrent._
import ExecutionContext.Implicits.global
import scala.concurrent.duration.{Duration, SECONDS}
import scala.util.{Failure, Success, Try}

final case class Tuple(_1: String, _2: Double)
final case class Author(_id: String, articles: Int, averageWords: Double, avgAmountOfSources: Double,
                        daysPublished: List[String], lastTexts: List[String], perDepartment: List[String],
                        score: Double, sentimentPerDay: List[String], sentimentPerDepartment: List[String])


object Author {

  implicit object AuthorReader extends BSONDocumentReader[Author] {
    override def readDocument(doc: BSONDocument): Try[Author] = {
      Try(Author(
        doc.getAsTry[String]("_id").get,
        doc.getAsTry[Int]("articles").get,
        doc.getAsTry[Double]("averageWords").get,
        doc.getAsTry[Double]("avgAmountOfSources").get,
        doc.getAsTry[List[String]]("daysPublished").get,
        doc.getAsTry[List[String]]("lastTexts").get,
        doc.getAsTry[List[String]]("perDepartment").get,
        doc.getAsTry[Double]("score").get,
        doc.getAsTry[List[String]]("sentimentPerDay").get,
        doc.getAsTry[List[String]]("sentimentPerDepartment").get
      ))
    }
  }

}

object DBConnector {

  def dbFromConnection(connection: MongoConnection): Future[BSONCollection] =
    connection.database("inews").map(_.collection("authors"))

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

  def findAuthorById(collection: BSONCollection, id: String): Option[BSONDocument] = {
    val query = BSONDocument("_id" -> id)
    val doc = collection.find(query).one[BSONDocument]
    Await.result(doc, Duration(1, SECONDS))
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
