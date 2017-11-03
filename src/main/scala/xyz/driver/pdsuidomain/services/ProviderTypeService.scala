package xyz.driver.pdsuidomain.services

import xyz.driver.core.rest.AuthorizedServiceRequestContext
import xyz.driver.entities.users.AuthUserInfo
import xyz.driver.pdsuicommon.db.Sorting
import xyz.driver.pdsuicommon.error.DomainError
import xyz.driver.pdsuidomain.entities.ProviderType

import scala.concurrent.Future

object ProviderTypeService {
  sealed trait GetListReply
  object GetListReply {
    final case class EntityList(xs: Seq[ProviderType], totalFound: Int) extends GetListReply
    case object AuthorizationError extends GetListReply with DomainError.AuthorizationError {
      def userMessage: String = "Access denied"
    }
  }
}

trait ProviderTypeService {

  import ProviderTypeService._

  def getAll(sorting: Option[Sorting] = None)(
          implicit requestContext: AuthorizedServiceRequestContext[AuthUserInfo]): Future[GetListReply]
}
