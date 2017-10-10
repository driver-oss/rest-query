package xyz.driver.pdsuidomain.formats.json.sprayformats

import java.time.LocalDateTime

import spray.json.{RootJsonFormat, _}
import xyz.driver.pdsuicommon.db.Pagination
import xyz.driver.pdsuidomain.formats.json.sprayformats.common._

final case class ListResponse[+T](items: Seq[T], meta: ListResponse.Meta)

object ListResponse extends DefaultJsonProtocol {
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

  private def listResponseJsonWriter[T: JsonWriter](listResponse: ListResponse[T]) = {
    JsObject(
      itemsField -> listResponse.items.map(_.toJson).toJson,
      metaField  -> listResponse.meta.toJson
    )
  }

  implicit def listResponseWriter[T: JsonWriter]: RootJsonWriter[ListResponse[T]] =
    new RootJsonWriter[ListResponse[T]] {
      override def write(listResponse: ListResponse[T]): JsValue = {
        listResponseJsonWriter(listResponse)
      }
    }

  implicit def listResponseFormat[T: RootJsonFormat]: RootJsonFormat[ListResponse[T]] =
    new RootJsonFormat[ListResponse[T]] {
      override def write(listResponse: ListResponse[T]): JsValue = {
        listResponseJsonWriter(listResponse)
      }

      override def read(json: JsValue): ListResponse[T] = json match {
        case JsObject(fields) =>
          val items = fields
            .get(itemsField)
            .map(_.convertTo[Seq[T]])
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
