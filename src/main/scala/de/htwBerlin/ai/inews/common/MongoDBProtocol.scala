package de.htwBerlin.ai.inews.common

import org.mongodb.scala.bson.ObjectId
import spray.json.{DefaultJsonProtocol, JsString, JsValue, RootJsonFormat}

object MongoDBProtocol extends DefaultJsonProtocol {
  implicit object ObjectIdSerializer extends RootJsonFormat[ObjectId] {
    override def write(obj: ObjectId): JsValue = {
      JsString(obj.toHexString)
    }

    override def read(json: JsValue): ObjectId = {
      val ob = new ObjectId(json.toString())
      ob
    }
  }
}


