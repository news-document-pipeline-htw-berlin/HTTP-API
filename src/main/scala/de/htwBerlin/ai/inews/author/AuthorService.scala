package de.htwBerlin.ai.inews.author

import akka.http.scaladsl.model.StatusCodes.NotFound
import akka.http.scaladsl.server.{Directives, Route}
import de.htwBerlin.ai.inews.author.JsonFormat._

import scala.concurrent.ExecutionContext

/**
 * Service for author related interactions.
 *
 * @param executionContext
 */
class AuthorService()(implicit executionContext: ExecutionContext) extends Directives {
  /**
   * Retrieves an author by id.
   *
   * @param id author name
   * @return Author result on success, HTTP 404 if not found
   */
  def getAuthor(id: String): Route = {
    val res = AuthorDBConnector.findAuthorById(id.replace("+", " "))
    if (res.isDefined)
      complete(res.get)
    else
      complete(NotFound)
  }
}