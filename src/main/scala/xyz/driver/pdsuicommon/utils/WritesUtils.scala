package xyz.driver.pdsuicommon.utils

import play.api.libs.json._

object WritesUtils {

  def filterKeys[T](p: String => Boolean)(implicit w: Writes[T]): Writes[T] = {
    filter {
      case (key, _) => p(key)
    }
  }

  def filter[T](p: (String, JsValue) => Boolean)(implicit w: Writes[T]): Writes[T] = {
    w.transform { input: JsValue =>
      input match {
        case JsObject(map) => JsObject(map.filter(Function.tupled(p)))
        case x => x
      }
    }
  }
}
