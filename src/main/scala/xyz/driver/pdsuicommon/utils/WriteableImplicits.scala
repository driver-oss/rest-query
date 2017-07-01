package xyz.driver.pdsuicommon.utils

import play.api.http.{ContentTypes, Writeable}
import play.api.libs.json.{Json, Writes}

// @TODO this should be an object with a method, that gets HTTP-headers and returns suitable Writeable
trait WriteableImplicits {

  // Write JSON by default at now
  implicit def defaultWriteable[T](implicit inner: Writes[T]) = Writeable[T](
    { x: T =>
      Writeable.writeableOf_JsValue.transform(Json.toJson(x))
    },
    Option(ContentTypes.JSON)
  )

}
