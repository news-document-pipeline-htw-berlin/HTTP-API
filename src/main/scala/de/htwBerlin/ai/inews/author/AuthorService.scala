package de.htwBerlin.ai.inews.author

import akka.http.scaladsl.model.StatusCodes.NotFound
import akka.http.scaladsl.server.{Directives, Route}
import de.htwBerlin.ai.inews.author.JsonFormat._

import scala.concurrent.{Await, ExecutionContext, Future}


class AuthorService()(implicit executionContext: ExecutionContext) extends Directives {
  /**
   * Retrieves an author by id.
   * @param id author name
   * @return Author result on success
   */
  def getAuthor(id: String): Route = {
    val res = AuthorDBConnector.findAuthorById(id.replace("+", " "))
    if (res.isEmpty)
      return complete(NotFound)
    complete(res.head)
  }
}