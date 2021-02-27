package de.htwBerlin.ai.inews.author

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.{DefaultJsonProtocol, RootJsonFormat}

/**
 * Enables parsing of author objects to and from JSON.
 */
object JsonFormat extends SprayJsonSupport with DefaultJsonProtocol {

  implicit val authorIntMapJsonFormat: RootJsonFormat[AuthorIntMap] =
    jsonFormat2(AuthorIntMap)

  implicit val authorDoubleMapJsonFormat: RootJsonFormat[AuthorDoubleMap] =
    jsonFormat2(AuthorDoubleMap)

  implicit val authorJsonFormat: RootJsonFormat[Author] =
    jsonFormat11(Author)

}

