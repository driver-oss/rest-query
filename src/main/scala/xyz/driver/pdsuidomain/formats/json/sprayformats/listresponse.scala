package xyz.driver.pdsuidomain.formats.json.sprayformats

import spray.json._
import spray.json.DefaultJsonProtocol._
import xyz.driver.pdsuidomain.ListResponse
import xyz.driver.pdsuidomain.formats.json.sprayformats.common._

object listresponse {
  private val itemsField = "items"
  private val metaField  = "meta"

  implicit val listResponseMetaFormat: RootJsonFormat[ListResponse.Meta] = jsonFormat4(ListResponse.Meta.apply)

  implicit def listResponseWriter[T: JsonWriter]: RootJsonWriter[ListResponse[T]] =
    new RootJsonWriter[ListResponse[T]] {
      override def write(listResponse: ListResponse[T]): JsValue = {
        JsObject(
          itemsField -> listResponse.items.map(_.toJson).toJson,
          metaField  -> listResponse.meta.toJson
        )
      }
    }

  implicit def listResponseReader[T: JsonReader]: RootJsonReader[ListResponse[T]] =
    new RootJsonReader[ListResponse[T]] {
      override def read(json: JsValue): ListResponse[T] = json match {
        case JsObject(fields) =>
          val items = fields
            .get(itemsField)
            .map {
              case JsArray(elements) => elements.map(_.convertTo[T])(collection.breakOut)
              case x                 => deserializationError(s"Expected Array as JsArray, but got $x")
            }
            .getOrElse(deserializationError(s"ListResponse json object does not contain `$itemsField` field: $json"))

          val meta = fields
            .get(metaField)
            .map(_.convertTo[ListResponse.Meta])
            .getOrElse(deserializationError(s"ListResponse json object does not contain `$metaField` field: $json"))

          ListResponse(items, meta)
        case _ => deserializationError(s"Expected ListResponse json object, but got $json")
      }
    }
}
