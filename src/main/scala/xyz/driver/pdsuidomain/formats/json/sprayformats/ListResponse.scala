package xyz.driver.pdsuidomain.formats.json.sprayformats

import java.time.LocalDateTime

import spray.json.{JsArray, RootJsonFormat, _}
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

  implicit def listResponseMetaWriter[T: RootJsonWriter]: RootJsonWriter[ListResponse[T]] =
    new RootJsonWriter[ListResponse[T]] {
      override def write(listResponse: ListResponse[T]): JsValue = {
        JsObject(
          itemsField -> JsArray(listResponse.items.map(_.toJson).toVector),
          metaField  -> listResponse.meta.toJson
        )
      }
    }
}
