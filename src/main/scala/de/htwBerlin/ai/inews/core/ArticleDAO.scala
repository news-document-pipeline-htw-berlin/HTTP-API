package de.htwBerlin.ai.inews.core

import de.htwBerlin.ai.inews.util.JSONParser

import scala.concurrent.{ExecutionContext, Future}

class ArticleDAO()(implicit executionContext: ExecutionContext) {
  // this method is only for testing and will be deleted later
  def fetchItem(file: String): Future[Array[Article]] = Future {
    val jsonMap = JSONParser.parse("testResourcesJSON/" + file)

    Array(new Article(jsonMap("title"), jsonMap("intro"), jsonMap("article"),
      jsonMap("author"), jsonMap("published"), jsonMap("summery"),
      jsonMap("url"), jsonMap("department")))
  }

  def getById(id: Any): Future[Article] = Future {
    ???
  }

  def getByDepartment(department: Any): Future[Array[Article]] = Future {
    ???
  }
}
