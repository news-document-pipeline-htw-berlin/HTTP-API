package de.htwBerlin.ai.inews.author

import  de.htwBerlin.ai.inews.common.AnyJsonFormat._
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.{DefaultJsonProtocol, RootJsonFormat}

object JsonFormat extends SprayJsonSupport with DefaultJsonProtocol {

  implicit val authorIntMapJsonFormat: RootJsonFormat[AuthorIntMap] =
    jsonFormat2(AuthorIntMap)

  implicit val authorDoubleMapJsonFormat: RootJsonFormat[AuthorDoubleMap] =
    jsonFormat2(AuthorDoubleMap)
  implicit val authorJsonFormat: RootJsonFormat[Author] =
    jsonFormat11(Author)

}

