package de.htwBerlin.ai.inews.common

import org.mongodb.scala.bson.ObjectId
import spray.json.{DefaultJsonProtocol, JsString, JsValue, RootJsonFormat}

/**
 * Enables (de-)serialization of mongoDB ObjectIds.
 */
object MongoDBProtocol extends DefaultJsonProtocol {
  implicit object ObjectIdSerializer extends RootJsonFormat[ObjectId] {
    /**
     * Serializes an object id.
     * @param obj ObjectId
     * @return object id (JSON)
     */
    override def write(obj: ObjectId): JsValue = {
      JsString(obj.toHexString)
    }

    /**
     * Deserialzes an object id.
     * @param json object id (JSON)
     * @return ObjectId
     */
    override def read(json: JsValue): ObjectId = {
      val ob = new ObjectId(json.toString())
      ob
    }
  }
}


