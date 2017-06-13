package xyz.driver.pdsuidomain.services

import xyz.driver.pdsuicommon.auth.AuthenticatedRequestContext
import xyz.driver.pdsuicommon.db.Sorting
import xyz.driver.pdsuicommon.error.DomainError
import xyz.driver.pdsuicommon.logging.PhiLogging
import xyz.driver.pdsuidomain.entities.Label

import scala.concurrent.Future

object LabelService {

  sealed trait GetListReply
  object GetListReply {
    case class EntityList(xs: Seq[Label], totalFound: Int) extends GetListReply

    case object AuthorizationError extends GetListReply with DomainError.AuthorizationError {
      def userMessage: String = "Access denied"
    }
  }
}

trait LabelService extends PhiLogging {
  import LabelService._

  def getAll(sorting: Option[Sorting] = None)(
          implicit requestContext: AuthenticatedRequestContext): Future[GetListReply]
}
