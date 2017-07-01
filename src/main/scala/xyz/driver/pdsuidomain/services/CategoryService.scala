package xyz.driver.pdsuidomain.services

import xyz.driver.pdsuicommon.auth.AuthenticatedRequestContext
import xyz.driver.pdsuicommon.db.Sorting
import xyz.driver.pdsuicommon.error.DomainError
import xyz.driver.pdsuidomain.entities.CategoryWithLabels

import scala.concurrent.Future

object CategoryService {
  sealed trait GetListReply
  object GetListReply {
    case class EntityList(xs: Seq[CategoryWithLabels], totalFound: Int) extends GetListReply

    case object AuthorizationError extends GetListReply with DomainError.AuthorizationError {
      def userMessage: String = "Access denied"
    }
  }
}

trait CategoryService {

  import CategoryService._

  def getAll(sorting: Option[Sorting] = None)(
          implicit requestContext: AuthenticatedRequestContext): Future[GetListReply]
}
