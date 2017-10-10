package xyz.driver.pdsuidomain.services.fake

import java.time.LocalDateTime

import akka.NotUsed
import akka.stream.scaladsl.Source
import akka.util.ByteString
import xyz.driver.core.generators
import xyz.driver.entities.labels.Label
import xyz.driver.entities.patient.CancerType
import xyz.driver.pdsuicommon.auth.AuthenticatedRequestContext
import xyz.driver.pdsuicommon.db._
import xyz.driver.pdsuicommon.domain.{LongId, StringId, UuidId}
import xyz.driver.pdsuidomain.entities.export.trial.{ExportTrialArm, ExportTrialLabelCriterion, ExportTrialWithLabels}
import xyz.driver.pdsuidomain.entities.{Arm, Criterion, Trial}
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
          implicit requestContext: AuthenticatedRequestContext): Future[Source[ByteString, NotUsed]] =
    Future.failed(new NotImplementedError("fake pdf download is not implemented"))

  def getAll(filter: SearchFilterExpr = SearchFilterExpr.Empty,
             sorting: Option[Sorting] = None,
             pagination: Option[Pagination] = None)(
          implicit requestContext: AuthenticatedRequestContext): Future[GetListReply] =
    Future.successful(GetListReply.EntityList(Seq(trial), 1, None))

  override def getTrialWithLabels(trialId: StringId[Trial], cancerType: CancerType)(
          implicit requestContext: AuthenticatedRequestContext): Future[GetTrialWithLabelsReply] =
    Future.successful(GetTrialWithLabelsReply.Entity(nextExportTrialWithLabels()))

  override def getTrialsWithLabels(cancerType: CancerType)(
          implicit requestContext: AuthenticatedRequestContext): Future[GetTrialsWithLabelsReply] =
    Future.successful(GetTrialsWithLabelsReply.EntityList(generators.seqOf(nextExportTrialWithLabels())))

  private def nextExportTrialWithLabels() =
    ExportTrialWithLabels(
      StringId[Trial]("NCT" + generators.nextInt(999999).toString),
      UuidId[Trial](generators.nextUuid()),
      generators.oneOf("adenocarcinoma", "breast", "prostate"),
      LocalDateTime.now(),
      labelVersion = 1L,
      generators.listOf(
        new ExportTrialArm(
          LongId[Arm](generators.nextInt(999999).toLong),
          generators.nextName().value
        )),
      generators.listOf(
        new ExportTrialLabelCriterion(
          LongId[Criterion](generators.nextInt(999999).toLong),
          generators.nextOption(generators.nextBoolean()),
          LongId[Label](generators.nextInt(999999).toLong),
          generators.setOf(LongId[Arm](generators.nextInt(999999).toLong)),
          generators.nextName().value,
          generators.nextBoolean(),
          generators.nextBoolean()
        ))
    )

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
