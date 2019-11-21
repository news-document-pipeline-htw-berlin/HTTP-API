package de.htwBerlin.ai.inews.util

import spray.json.DefaultJsonProtocol._
import spray.json._

import scala.io.Source

object JSONParser {
  def parse(filename: String) : Map[String, String] = {
    val file = Source.fromFile(filename)
    val json = file.getLines.mkString
    file.close

    val jsonAst = json.parseJson
    val data = jsonAst.convertTo[Map[String, JsValue]]

    data.map(value => value._2 match {
        case JsString(s) => (value._1, s)
        case _ => (value._1, "")
    })
  }
}
