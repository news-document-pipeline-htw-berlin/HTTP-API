package de.htwBerlin.ai.inews.http.routes

import akka.http.scaladsl.server.{Directives, Route}
import de.htwBerlin.ai.inews.author.AuthorService

import scala.concurrent.ExecutionContext

class Authors(authorService: AuthorService)(implicit executionContext: ExecutionContext) extends Directives {

  final val route: Route = {
    pathPrefix("authors") {
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
