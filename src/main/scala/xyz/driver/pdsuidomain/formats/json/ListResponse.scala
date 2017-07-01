package xyz.driver.pdsuidomain.formats.json

import java.time.LocalDateTime

import xyz.driver.pdsuicommon.db.Pagination
import xyz.driver.pdsuicommon.json.Serialization.seqJsonFormat
import play.api.libs.functional.syntax._
import play.api.libs.json._

case class ListResponse[+T](items: Seq[T], meta: ListResponse.Meta)

object ListResponse {

  case class Meta(itemsCount: Int, pageNumber: Int, pageSize: Int, lastUpdate: Option[LocalDateTime])

  object Meta {
    def apply(itemsCount: Int, pagination: Pagination, lastUpdate: Option[LocalDateTime]): Meta = {
      Meta(itemsCount, pagination.pageNumber, pagination.pageSize, lastUpdate)
    }
  }

  private val listResponseMetaJsonReads: Reads[Meta] = {
    ((JsPath \ "itemsCount").read[Int] and
      (JsPath \ "pageNumber").read[Int] and
      (JsPath \ "pageSize").read[Int] and
      (JsPath \ "lastUpdate").readNullable[LocalDateTime]).apply {
      (itemsCount: Int, pageNumber: Int, pageSize: Int, lastUpdate: Option[LocalDateTime]) =>
        Meta(itemsCount, pageNumber, pageSize, lastUpdate)
    }
  }

  implicit val listResponseMetaJsonWrites: Writes[Meta] = (
    (JsPath \ "itemsCount").write[Int] and
      (JsPath \ "pageNumber").write[Int] and
      (JsPath \ "pageSize").write[Int] and
      (JsPath \ "lastUpdate").write[Option[LocalDateTime]]
  )(unlift(Meta.unapply))

  implicit val listResponseMetaJsonFormat: Format[Meta] = Format(
    listResponseMetaJsonReads,
    listResponseMetaJsonWrites
  )

  implicit def listResponseJsonWrites[T](implicit f: Writes[T]): Writes[ListResponse[T]] =
    (
      (JsPath \ "items").write[Seq[T]] and
        (JsPath \ "meta").write[Meta]
    )(unlift(ListResponse.unapply[T]))

  implicit def listResponseJsonFormat[T](implicit f: Format[T]): Format[ListResponse[T]] =
    (
      (JsPath \ "items").format(seqJsonFormat[T]) and
        (JsPath \ "meta").format[Meta]
    )(ListResponse.apply[T], unlift(ListResponse.unapply[T]))

}
