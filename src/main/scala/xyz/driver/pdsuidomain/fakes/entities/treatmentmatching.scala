package xyz.driver.pdsuidomain.fakes.entities

import eu.timepit.refined.numeric.NonNegative
import xyz.driver.entities.labels.Label
import xyz.driver.fakes
import xyz.driver.pdsuicommon.domain.{LongId, StringId, User}
import xyz.driver.pdsuidomain.ListResponse
import xyz.driver.pdsuidomain.entities._
import xyz.driver.pdsuidomain.services.PatientCriterionService.{DraftPatientCriterion, RichPatientCriterion}
import xyz.driver.pdsuidomain.services.PatientEligibleTrialService.RichPatientEligibleTrial
import xyz.driver.pdsuidomain.services.PatientHypothesisService.RichPatientHypothesis
import xyz.driver.pdsuidomain.services.PatientLabelService.RichPatientLabel
import eu.timepit.refined.{refineV, refineMV}

object treatmentmatching {
  import common._
  import xyz.driver.core.generators

  final case class DraftPatientCriterionList(list: List[DraftPatientCriterion])
  object DraftPatientCriterionList {
    import spray.json._
    import xyz.driver.pdsuidomain.formats.json.patientcriterion._

    implicit val draftPatientCriterionListFormat: RootJsonFormat[DraftPatientCriterionList] =
      new RootJsonFormat[DraftPatientCriterionList] {
        override def write(obj: DraftPatientCriterionList): JsValue = {
          JsArray(obj.list.map(_.toJson).toVector)
        }

        override def read(json: JsValue): DraftPatientCriterionList = json match {
          case j: JsObject =>
            DraftPatientCriterionList(draftPatientCriterionListReader.read(j))

          case _ => deserializationError(s"Expected List of DraftPatientCriterion json object, but got $json")
        }
      }
  }

  def nextPatientOrderId: PatientOrderId = PatientOrderId(generators.nextUuid())

  def nextPatientAction: PatientHistory.Action =
    generators.oneOf[PatientHistory.Action](PatientHistory.Action.All)

  def nextPatientState: PatientHistory.State =
    generators.oneOf[PatientHistory.State](PatientHistory.State.All)

  def nextPatient(): Patient = Patient(
    id = nextUuidId[Patient],
    status = nextPatientStatus,
    name = nextFullName[Patient],
    dob = nextLocalDate,
    assignee = generators.nextOption(nextStringId[User]),
    previousStatus = generators.nextOption(generators.oneOf[Patient.Status](Patient.Status.AllPrevious)),
    previousAssignee = generators.nextOption(nextStringId[User]),
    lastActiveUserId = generators.nextOption(nextStringId[User]),
    isUpdateRequired = generators.nextBoolean(),
    disease = nextCancerType,
    orderId = nextPatientOrderId,
    lastUpdate = nextLocalDateTime
  )

  def nextPatientLabel(): PatientLabel = PatientLabel(
    id = nextLongId[PatientLabel],
    patientId = nextUuidId[Patient],
    labelId = nextLongId[Label],
    score = generators.nextInt(100),
    primaryValue = generators.nextOption(fakes.entities.labels.nextLabelValue()),
    verifiedPrimaryValue = generators.nextOption(fakes.entities.labels.nextLabelValue()),
    isImplicitMatch = generators.nextBoolean(),
    isVisible = generators.nextBoolean()
  )

  def nextRichPatientLabel(): RichPatientLabel = RichPatientLabel(
    patientLabel = nextPatientLabel(),
    isVerified = generators.nextBoolean()
  )

  def nextPatientCriterion(): PatientCriterion = PatientCriterion(
    id = nextLongId[PatientCriterion],
    patientLabelId = nextLongId[PatientLabel],
    trialId = generators.nextInt(Int.MaxValue).toLong,
    nctId = nextStringId[Trial],
    criterionId = nextLongId[Criterion],
    criterionText = generators.nextString(),
    criterionValue = generators.nextOption(generators.nextBoolean()),
    criterionIsDefining = generators.nextBoolean(),
    eligibilityStatus = generators.nextOption(fakes.entities.labels.nextLabelValue()),
    verifiedEligibilityStatus = generators.nextOption(fakes.entities.labels.nextLabelValue()),
    isVerified = generators.nextBoolean(),
    isVisible = generators.nextBoolean(),
    lastUpdate = nextLocalDateTime,
    inclusion = generators.nextOption(generators.nextBoolean())
  )

  def nextDraftPatientCriterion(): DraftPatientCriterion = DraftPatientCriterion(
    id = nextLongId[PatientCriterion],
    eligibilityStatus = generators.nextOption(fakes.entities.labels.nextLabelValue()),
    isVerified = generators.nextOption(generators.nextBoolean())
  )

  def nextDraftPatientCriterionList(): DraftPatientCriterionList = {
    DraftPatientCriterionList(List.fill(3)(nextDraftPatientCriterion()))
  }

  def nextPatientCriterionArm(criterionId: LongId[PatientCriterion]): PatientCriterionArm = PatientCriterionArm(
    patientCriterionId = criterionId,
    armId = nextLongId[Arm],
    armName = generators.nextString()
  )

  def nextRichPatientCriterion(): RichPatientCriterion = {
    val patientCriterion = nextPatientCriterion()
    RichPatientCriterion(
      patientCriterion = patientCriterion,
      labelId = nextLongId[Label],
      armList = List(
        nextPatientCriterionArm(patientCriterion.id),
        nextPatientCriterionArm(patientCriterion.id),
        nextPatientCriterionArm(patientCriterion.id)
      )
    )
  }

  def nextPatientLabelEvidenceView(): PatientLabelEvidenceView = PatientLabelEvidenceView(
    id = nextLongId[PatientLabelEvidence],
    value = fakes.entities.labels.nextLabelValue(),
    evidenceText = generators.nextString(),
    documentId = generators.nextOption(nextLongId[Document]),
    evidenceId = generators.nextOption(nextLongId[ExtractedData]),
    reportId = generators.nextOption(nextUuidId[DirectReport]),
    documentType = nextDocumentType(),
    date = generators.nextOption(nextLocalDate),
    providerType = nextProviderType(),
    patientId = nextUuidId[Patient],
    labelId = nextLongId[Label],
    isImplicitMatch = generators.nextBoolean()
  )

  def nextPatientTrialArmGroupView(trialId: StringId[Trial]): PatientTrialArmGroupView = PatientTrialArmGroupView(
    id = nextLongId[PatientTrialArmGroup],
    patientId = nextUuidId[Patient],
    trialId = trialId,
    hypothesisId = nextUuidId[Hypothesis],
    eligibilityStatus = generators.nextOption(fakes.entities.labels.nextLabelValue()),
    verifiedEligibilityStatus = generators.nextOption(fakes.entities.labels.nextLabelValue()),
    isVerified = generators.nextBoolean()
  )

  def nextRichPatientEligibleTrial(): RichPatientEligibleTrial = {
    val patientCriterionId = nextLongId[PatientCriterion]
    val trial              = trialcuration.nextTrial()
    RichPatientEligibleTrial(
      trial = trial,
      group = nextPatientTrialArmGroupView(trial.id),
      arms = List(
        nextPatientCriterionArm(patientCriterionId),
        nextPatientCriterionArm(patientCriterionId),
        nextPatientCriterionArm(patientCriterionId)
      )
    )
  }

  def nextPatientHypothesis(): PatientHypothesis = PatientHypothesis(
    id = nextUuidId[PatientHypothesis],
    patientId = nextUuidId[Patient],
    hypothesisId = nextUuidId[Hypothesis],
    rationale = Option(generators.nextString()),
    matchedTrials = refineV[NonNegative](generators.nextInt(Int.MaxValue).toLong) match {
      case Left(_)            => refineMV[NonNegative](0)
      case Right(nonNegative) => nonNegative
    }
  )

  def nextRichPatientHypothesis(): RichPatientHypothesis = RichPatientHypothesis(
    patientHypothesis = nextPatientHypothesis(),
    isRequired = generators.nextBoolean()
  )

  def nextPatientIssue(): PatientIssue = PatientIssue(
    id = nextLongId[PatientIssue],
    userId = nextStringId[User],
    patientId = nextUuidId[Patient],
    lastUpdate = nextLocalDateTime,
    isDraft = generators.nextBoolean(),
    text = generators.nextString(),
    archiveRequired = generators.nextBoolean()
  )

  def nextPatientHistory(): PatientHistory = PatientHistory(
    id = nextLongId[PatientHistory],
    executor = nextStringId[User],
    patientId = nextUuidId[Patient],
    state = nextPatientState,
    action = nextPatientAction,
    created = nextLocalDateTime
  )

  def nextPatientListResponse(): ListResponse[Patient] = {
    val xs: Seq[Patient] = Seq.fill(3)(nextPatient())
    nextListResponse(xs)
  }

  def nextRichPatientLabelListResponse(): ListResponse[RichPatientLabel] = {
    val xs: Seq[RichPatientLabel] = Seq.fill(3)(nextRichPatientLabel())
    nextListResponse(xs)
  }

  def nextPatientLabelListResponse(): ListResponse[PatientLabel] = {
    val xs: Seq[PatientLabel] = Seq.fill(3)(nextPatientLabel())
    nextListResponse(xs)
  }

  def nextRichPatientCriterionListResponse(): ListResponse[RichPatientCriterion] = {
    val xs: Seq[RichPatientCriterion] = Seq.fill(3)(nextRichPatientCriterion())
    nextListResponse(xs)
  }

  def nextRichPatientEligibleTrialListResponse(): ListResponse[RichPatientEligibleTrial] = {
    val xs: Seq[RichPatientEligibleTrial] = Seq.fill(3)(nextRichPatientEligibleTrial())
    nextListResponse(xs)
  }

  def nextRichPatientHypothesisListResponse(): ListResponse[RichPatientHypothesis] = {
    val xs: Seq[RichPatientHypothesis] = Seq.fill(3)(nextRichPatientHypothesis())
    nextListResponse(xs)
  }

  def nextPatientLabelEvidenceViewListResponse(): ListResponse[PatientLabelEvidenceView] = {
    val xs: Seq[PatientLabelEvidenceView] = Seq.fill(3)(nextPatientLabelEvidenceView())
    nextListResponse(xs)
  }

  def nextPatientIssuesListResponse(): ListResponse[PatientIssue] = {
    val xs: Seq[PatientIssue] = Seq.fill(3)(nextPatientIssue())
    nextListResponse(xs)
  }

  def nextPatientHistoryListResponse(): ListResponse[PatientHistory] = {
    val xs: Seq[PatientHistory] = Seq.fill(3)(nextPatientHistory())
    nextListResponse(xs)
  }

}
