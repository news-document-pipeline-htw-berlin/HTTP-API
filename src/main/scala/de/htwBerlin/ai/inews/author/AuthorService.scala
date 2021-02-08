package de.htwBerlin.ai.inews.author

import java.util.concurrent.TimeUnit
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.model.StatusCodes.NotFound
import akka.http.scaladsl.model.headers.HttpCookie
import akka.http.scaladsl.server.Directives.complete
import akka.http.scaladsl.server.{Directives, Route}
import authentikat.jwt.{JsonWebToken, JwtClaimsSet, JwtHeader}
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
  /**
   * Retrieves an author by id.
   * @param id author name
   * @return Author result on success
   */
  def getAuthor(id: String): Route = {
    val res = AuthorDBConnector.findAuthorById(id)
    res.getOrElse(NotFound) match {
      case BSONDocument(i) => complete(BSONDocument.pretty(res.get))
      case None => complete(StatusCodes.NotFound, "Author " + id + " could not be found.")
      case NotFound => complete(StatusCodes.NotFound, "Author " + id + " could not be found.")
    }
  }
}