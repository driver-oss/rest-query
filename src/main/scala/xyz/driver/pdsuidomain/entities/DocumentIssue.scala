package xyz.driver.pdsuidomain.entities

import java.time.LocalDateTime

import xyz.driver.pdsuicommon.domain.{LongId, StringId, User}
import xyz.driver.pdsuicommon.logging._

final case class DocumentIssue(id: LongId[DocumentIssue],
                               userId: StringId[User],
                               documentId: LongId[Document],
                               startPage: Option[Double],
                               endPage: Option[Double],
                               lastUpdate: LocalDateTime,
                               isDraft: Boolean,
                               text: String,
                               archiveRequired: Boolean)

object DocumentIssue {
  implicit def toPhiString(x: DocumentIssue): PhiString = {
    import x._
    phi"DocumentIssue(id=$id, userId=$userId, documentId=$documentId)"
  }
}
