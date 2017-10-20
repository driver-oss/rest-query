package xyz.driver.pdsuidomain

import java.time.LocalDateTime

import xyz.driver.pdsuicommon.db.Pagination

final case class ListResponse[+T](items: Seq[T], meta: ListResponse.Meta)

object ListResponse {

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

}
