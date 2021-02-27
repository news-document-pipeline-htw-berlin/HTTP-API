package de.htwBerlin.ai.inews.http.routes

import akka.http.scaladsl.server.{Directives, Route}
import de.htwBerlin.ai.inews.author.AuthorService

import scala.concurrent.ExecutionContext

/**
 * Defines routes associated with authors.
 *
 * @param authorService
 * @param executionContext
 */
class Authors(authorService: AuthorService)(implicit executionContext: ExecutionContext) extends Directives {

  final val route: Route = {
    pathPrefix("authors") {
      // /api/authors?id={id}
      // GET author by id
      get {
        parameters(
          "id".as[String]
        ) {
          id => authorService.getAuthor(id)
        }
      }
    }
  }
}
