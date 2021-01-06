package de.htwBerlin.ai.inews.author

import java.util.concurrent.TimeUnit
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.model.StatusCodes.NotFound
import akka.http.scaladsl.model.headers.HttpCookie
import akka.http.scaladsl.server.Directives.complete
import akka.http.scaladsl.server.{Directives, Route}
import authentikat.jwt.{JsonWebToken, JwtClaimsSet, JwtHeader}
import de.htwBerlin.ai.inews.DBConnector
import de.htwBerlin.ai.inews.{Author}
import org.mindrot.jbcrypt.BCrypt
import reactivemongo.api.{AsyncDriver, MongoConnection}
import spray.json.{DefaultJsonProtocol, RootJsonFormat}
import org.json4s.DefaultFormats
import org.json4s.native.Serialization.write
import reactivemongo.api.bson.BSONHandler.provided
import reactivemongo.api.bson.collection.BSONCollection
import reactivemongo.api.bson.{BSONDocument, BSONDocumentReader, BSONDocumentWriter, BSONHandler, Macros}
import reactivemongo.core.nodeset.Authenticate
import spray.json.DefaultJsonProtocol.listFormat

import scala.concurrent.duration.{Duration, SECONDS}
import scala.concurrent.{Await, ExecutionContext, Future}


class AuthorService()(implicit executionContext: ExecutionContext) extends Directives {

  val mongoUri = "mongodb://127.0.0.1:27017/inews"
  val driver = new AsyncDriver()

  val database = for {
    uri <- MongoConnection.fromString(mongoUri)
    con <- driver.connect(uri)
    dn <- Future(uri.db.get)
    db <- con.database(dn)
  } yield db

  implicit val formats: DefaultFormats = DefaultFormats
  implicit val authorHandler: BSONHandler[Author] = Macros.handler[Author]

  def getAuthor(id: String): Route = {
    val collection = DBConnector.dbFromConnection(Await.result(database.map(_.connection),
          Duration(1, SECONDS)))
    val res = DBConnector.findAuthorById(Await.result(collection, Duration(1, SECONDS)), id)

    res.getOrElse(NotFound) match {
      case BSONDocument(i) => complete(BSONDocument.pretty(res.get))
      case None => complete(StatusCodes.NotFound, "Author " + id + " could not be found.")
      case NotFound => complete(StatusCodes.NotFound, "Author " + id + " could not be found.")
    }
  }
}