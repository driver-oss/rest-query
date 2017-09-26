package xyz.driver.pdsuidomain.fakes.entities

import xyz.driver.pdsuicommon.domain.{LongId, User}
import xyz.driver.pdsuidomain.entities._
import xyz.driver.pdsuidomain.services.CriterionService.RichCriterion

object trialcuration {
  import xyz.driver.core.generators
  import common._

  def nextTrial(): Trial = Trial(
    id = nextStringId[Trial],
    externalId = nextUuidId[Trial],
    status = nextTrialStatus,
    assignee = Option(nextStringId[User]),
    previousStatus = Option(nextPreviousTrialStatus),
    previousAssignee = Option(nextStringId[User]),
    lastActiveUserId = Option(nextStringId[User]),
    lastUpdate = nextLocalDateTime,
    condition = nextCondition,
    phase = generators.nextString(),
    hypothesisId = Option(nextUuidId[Hypothesis]),
    studyDesignId = Option(nextLongId[StudyDesign]),
    originalStudyDesign = Option(generators.nextString()),
    isPartner = generators.nextBoolean(),
    overview = Option(generators.nextString()),
    overviewTemplate = generators.nextString(),
    isUpdated = generators.nextBoolean(),
    title = generators.nextString(),
    originalTitle = generators.nextString()
  )

  def nextArm(): Arm = Arm(
    id = nextLongId[Arm],
    name = generators.nextString(),
    originalName = generators.nextString(),
    trialId = nextStringId[Trial],
    deleted = Option(nextLocalDateTime)
  )

  def nextCriterion(): Criterion = Criterion(
    id = nextLongId[Criterion],
    trialId = nextStringId[Trial],
    text = Option(generators.nextString()),
    isCompound = generators.nextBoolean(),
    meta = generators.nextString()
  )

  def nextCriterionLabel(criterionId: LongId[Criterion]): CriterionLabel = CriterionLabel(
    id = nextLongId[CriterionLabel],
    labelId = Option(nextLongId[Label]),
    criterionId = criterionId,
    categoryId = Option(nextLongId[Category]),
    value = Option(generators.nextBoolean()),
    isDefining = generators.nextBoolean()
  )

  def nextRichCriterion(): RichCriterion = {
    val criterion = nextCriterion()
    RichCriterion(
      criterion = criterion,
      armIds = Seq(nextLongId[Arm], nextLongId[Arm]),
      labels = Seq(
        nextCriterionLabel(criterion.id),
        nextCriterionLabel(criterion.id)
      )
    )
  }

  def nextIntervention(): Intervention = Intervention(
    id = nextLongId[Intervention],
    trialId = nextStringId[Trial],
    name = generators.nextString(),
    originalName = generators.nextString(),
    typeId = Option(nextLongId[InterventionType]),
    originalType = Option(generators.nextString()),
    dosage = generators.nextString(),
    originalDosage = generators.nextString(),
    isActive = generators.nextBoolean()
  )

  def nextInterventionArm(interventionId: LongId[Intervention]): InterventionArm = InterventionArm(
    interventionId = interventionId,
    armId = nextLongId[SlotArm]
  )

  def nextInterventionWithArms(): InterventionWithArms = {
    val intervention = nextIntervention()
    InterventionWithArms(
      intervention = intervention,
      arms = List(
        nextInterventionArm(intervention.id),
        nextInterventionArm(intervention.id),
        nextInterventionArm(intervention.id)
      )
    )
  }

  def nextTrialIssue(): TrialIssue = TrialIssue(
    id = nextLongId[TrialIssue],
    userId = nextStringId[User],
    trialId = nextStringId[Trial],
    lastUpdate = nextLocalDateTime,
    isDraft = generators.nextBoolean(),
    text = generators.nextString(),
    evidence = generators.nextString(),
    archiveRequired = generators.nextBoolean(),
    meta = generators.nextString()
  )

  def nextTrialHistory(): TrialHistory = TrialHistory(
    id = nextLongId[TrialHistory],
    executor = nextStringId[User],
    trialId = nextStringId[Trial],
    state = nextTrialState,
    action = nextTrialAction,
    created = nextLocalDateTime
  )

  def nextHypothesis(): Hypothesis = Hypothesis(
    id = nextUuidId[Hypothesis],
    name = generators.nextString(),
    treatmentType = generators.nextString(),
    description = generators.nextString()
  )

  def nextStudyDesign(): StudyDesign = StudyDesign(
    id = nextLongId[StudyDesign],
    name = generators.nextString()
  )

  def nextInterventionType(): InterventionType = InterventionType(
    id = nextLongId[InterventionType],
    name = generators.nextString()
  )

}
