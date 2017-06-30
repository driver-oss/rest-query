package xyz.driver.pdsuidomain.services

import xyz.driver.pdsuicommon.auth.AuthenticatedRequestContext
import xyz.driver.pdsuicommon.db.Sorting
import xyz.driver.pdsuicommon.error.DomainError
import xyz.driver.pdsuidomain.entities.DocumentType
import xyz.driver.pdsuidomain.services.DocumentTypeService.GetListReply

import scala.concurrent.Future

object DocumentTypeService {
  sealed trait GetListReply
  object GetListReply {
    case class EntityList(xs: Seq[DocumentType], totalFound: Int) extends GetListReply
    case object AuthorizationError extends GetListReply with DomainError.AuthorizationError {
      def userMessage: String = "Access denied"
    }
  }
}

trait DocumentTypeService {

  def getAll(sorting: Option[Sorting] = None)(
          implicit requestContext: AuthenticatedRequestContext): Future[GetListReply]
}
