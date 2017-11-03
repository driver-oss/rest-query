package xyz.driver.pdsuidomain.entities

import java.time.LocalDateTime

import xyz.driver.core.auth.User
import xyz.driver.pdsuicommon.domain.LongId
import xyz.driver.pdsuicommon.logging._

final case class DocumentIssue(id: LongId[DocumentIssue],
                               userId: xyz.driver.core.Id[User],
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
    phi"DocumentIssue(id=$id, userId=${Unsafe(userId)}, documentId=$documentId)"
  }
}
