package xyz.driver.pdsuidomain.services

import xyz.driver.pdsuicommon.error.DomainError
import xyz.driver.pdsuidomain.entities.ScrapedTrial

import scala.concurrent.Future

object ScrapedTrialsService {

  sealed trait GetRawTrialReply
  object GetRawTrialReply {
    type Error = GetRawTrialReply with DomainError

    final case class TrialRawEntity(rawTrial: ScrapedTrial) extends GetRawTrialReply

    case object NotFoundError extends GetRawTrialReply with DomainError.NotFoundError {
      override def userMessage: String = "Raw clinical trial not found"
    }
  }

  sealed trait GetRawTrialOptReply
  object GetRawTrialOptReply {
    final case class TrialRawEntity(rawTrial: Option[ScrapedTrial]) extends GetRawTrialOptReply
  }

  sealed trait GetAllRawTrialsExceptReply
  object GetAllRawTrialsExceptReply {
    final case class MultipleRawTrials(rawTrials: Seq[ScrapedTrial]) extends GetAllRawTrialsExceptReply
  }

  sealed trait GetHtmlForReply
  object GetHtmlForReply {
    type TrialHtmlMap = Map[String, String]

    /**
      * @param trialHtmlMap nctId -> html
      */
    final case class HtmlMap(trialHtmlMap: TrialHtmlMap) extends GetHtmlForReply
  }
}

trait ScrapedTrialsService {

  import ScrapedTrialsService._

  def getRawTrial(nctId: String): Future[GetRawTrialReply]

  def getRawTrialOpt(nctId: String): Future[GetRawTrialOptReply]

  def getAllRawTrialsExcept(nctIds: Seq[String], limit: Int): Future[GetAllRawTrialsExceptReply]

  def getHtmlFor(nctIds: Set[String]): Future[GetHtmlForReply]
}
