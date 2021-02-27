package de.htwBerlin.ai.inews.common

import spray.json.{JsFalse, JsNumber, JsString, JsTrue, JsValue, JsonFormat}

object AnyJsonFormat extends JsonFormat[Any] {
  def write(x: Any) = x match {
    case n: Int => JsNumber(n)
    case s: String => JsString(s)
    case b: Boolean if b == true => JsTrue
    case b: Boolean if b == false => JsFalse
  }

  def read(value: JsValue) = value match {
    case JsNumber(n) => n.intValue()
    case JsString(s) => s
    case JsTrue => true
    case JsFalse => false
  }
}