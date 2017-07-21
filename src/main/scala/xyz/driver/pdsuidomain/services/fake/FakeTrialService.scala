package xyz.driver.pdsuidomain.services.fake

import java.time.LocalDateTime
import xyz.driver.pdsuicommon.auth.AuthenticatedRequestContext
import xyz.driver.pdsuicommon.db._
import xyz.driver.pdsuicommon.domain.{StringId, UuidId}
import xyz.driver.pdsuidomain.entities.Trial
import xyz.driver.pdsuidomain.services.TrialService
import scala.concurrent.Future

class FakeTrialService extends TrialService {

  import TrialService._

  private val trial = Trial(
    id = StringId(""),
    externalId = UuidId(),
    status = Trial.Status.New,
    assignee = None,
    previousStatus = None,
    previousAssignee = None,
    lastActiveUserId = None,
    lastUpdate = LocalDateTime.now(),
    condition = Trial.Condition.Breast,
    phase = "",
    hypothesisId = None,
    studyDesignId = None,
    originalStudyDesign = None,
    isPartner = false,
    overview = None,
    overviewTemplate = "",
    isUpdated = false,
    title = "",
    originalTitle = ""
  )

  def getById(id: StringId[Trial])(implicit requestContext: AuthenticatedRequestContext): Future[GetByIdReply] =
    Future.successful(
      GetByIdReply.Entity(trial)
    )

  def getPdfSource(trialId: StringId[Trial])(
          implicit requestContext: AuthenticatedRequestContext): Future[GetPdfSourceReply] =
    Future.failed(new NotImplementedError("fake pdf download is not implemented"))

  def getAll(filter: SearchFilterExpr = SearchFilterExpr.Empty,
             sorting: Option[Sorting] = None,
             pagination: Option[Pagination] = None)(
          implicit requestContext: AuthenticatedRequestContext): Future[GetListReply] =
    Future.successful(GetListReply.EntityList(Seq(trial), 1, None))

  def update(origTrial: Trial, draftTrial: Trial)(
          implicit requestContext: AuthenticatedRequestContext): Future[UpdateReply] =
    Future.successful(UpdateReply.Updated(draftTrial))

  def start(origTrial: Trial)(implicit requestContext: AuthenticatedRequestContext): Future[UpdateReply] =
    update(origTrial, origTrial)

  def submit(origTrial: Trial)(implicit requestContext: AuthenticatedRequestContext): Future[UpdateReply] =
    update(origTrial, origTrial)

  def restart(origTrial: Trial)(implicit requestContext: AuthenticatedRequestContext): Future[UpdateReply] =
    update(origTrial, origTrial)

  def flag(origTrial: Trial)(implicit requestContext: AuthenticatedRequestContext): Future[UpdateReply] =
    update(origTrial, origTrial)

  def resolve(origTrial: Trial)(implicit requestContext: AuthenticatedRequestContext): Future[UpdateReply] =
    update(origTrial, origTrial)

  def archive(origTrial: Trial)(implicit requestContext: AuthenticatedRequestContext): Future[UpdateReply] =
    update(origTrial, origTrial)

  def unassign(origTrial: Trial)(implicit requestContext: AuthenticatedRequestContext): Future[UpdateReply] =
    update(origTrial, origTrial)

}
