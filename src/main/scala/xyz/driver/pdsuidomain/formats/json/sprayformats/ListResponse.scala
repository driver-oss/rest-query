package xyz.driver.pdsuidomain.formats.json.sprayformats

import java.time.LocalDateTime

import spray.json._
import spray.json.DefaultJsonProtocol._
import xyz.driver.pdsuicommon.db.Pagination
import xyz.driver.pdsuidomain.formats.json.sprayformats.common._

final case class ListResponse[+T](items: Seq[T], meta: ListResponse.Meta)

object ListResponse {
  private val itemsField = "items"
  private val metaField  = "meta"

  final case class Meta(itemsCount: Int, pageNumber: Int, pageSize: Int, lastUpdate: Option[LocalDateTime])

  object Meta {
    def apply(itemsCount: Int, pagination: Pagination, lastUpdate: Option[LocalDateTime]): Meta = {
      Meta(
        itemsCount,
        pagination.pageNumber,
        pagination.pageSize,
        lastUpdate
      )
    }
  }

  implicit val listResponseMetaFormat: RootJsonFormat[Meta] = jsonFormat4(Meta.apply)

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
            .map(_.convertTo[Meta])
            .getOrElse(deserializationError(s"ListResponse json object does not contain `$metaField` field: $json"))

          ListResponse(items, meta)
        case _ => deserializationError(s"Expected ListResponse json object, but got $json")
      }
    }
}
