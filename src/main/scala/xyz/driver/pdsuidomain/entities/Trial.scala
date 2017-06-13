package xyz.driver.pdsuidomain.entities

import java.nio.file.Path
import java.time.LocalDateTime

import xyz.driver.pdsuicommon.domain.{LongId, StringId, User, UuidId}
import xyz.driver.pdsuicommon.logging._
import xyz.driver.pdsuicommon.utils.Utils
import xyz.driver.pdsuidomain.entities.Trial.{Condition, Status}

final case class StudyDesign(id: LongId[StudyDesign], name: String)

object StudyDesign {
  implicit def toPhiString(x: StudyDesign): PhiString = {
    import x._
    phi"StudyDesign(id=$id, category=${Unsafe(name)})"
  }
}

object Trial {

  sealed trait Status {
    def oneOf(xs: Status*): Boolean     = xs.contains(this)
    def oneOf(xs: Set[Status]): Boolean = xs.contains(this)
  }

  object Status {
    case object New            extends Status
    case object ReviewSummary  extends Status
    case object Summarized     extends Status
    case object PendingUpdate  extends Status
    case object Update         extends Status
    case object ReviewCriteria extends Status
    case object Done           extends Status
    case object Flagged        extends Status
    case object Archived       extends Status

    val All = Set[Status](
      New,
      ReviewSummary,
      Summarized,
      PendingUpdate,
      Update,
      ReviewCriteria,
      Done,
      Flagged,
      Archived
    )

    val AllPrevious = Set[Status](New, ReviewSummary, Summarized, ReviewCriteria)

    implicit def toPhiString(x: Status): PhiString = Unsafe(Utils.getClassSimpleName(x.getClass))
  }

  case class PdfSource(path: Path) extends AnyVal

  implicit def toPhiString(x: Trial): PhiString = {
    import x._
    phi"Trial(id=$id, externalId=$externalId, status=$status, previousStatus=$previousStatus, " +
      phi"assignee=$assignee, previousAssignee=$previousAssignee, isSummaryReviewed=$isSummaryReviewed, " +
      phi"isCriteriaReviewed=$isCriteriaReviewed)"
  }

  case class Locations(locations: List[String])

  sealed trait Condition

  object Condition {

    case object Breast   extends Condition
    case object Lung     extends Condition
    case object Prostate extends Condition

    val All = Set(Breast, Lung, Prostate)
  }
}

final case class Trial(id: StringId[Trial],
                       externalId: UuidId[Trial],
                       status: Status,
                       assignee: Option[LongId[User]],
                       previousStatus: Option[Status],
                       previousAssignee: Option[LongId[User]],
                       lastUpdate: LocalDateTime,
                       condition: Condition,
                       phase: String,
                       hypothesisId: Option[UuidId[Hypothesis]],
                       studyDesignId: Option[LongId[StudyDesign]],
                       originalStudyDesign: Option[String],
                       isPartner: Boolean,
                       overview: Option[String],
                       overviewTemplate: String,
                       isUpdated: Boolean,
                       title: String,
                       originalTitle: String,
                       isSummaryReviewed: Boolean,
                       isCriteriaReviewed: Boolean,
                       eligibilityCriteriaChecksum: String,
                       briefSummaryChecksum: String,
                       detailedDescriptionChecksum: String,
                       armDescriptionChecksum: String) {

  import Trial.Status._

  if (previousStatus.nonEmpty) {
    assert(AllPrevious.contains(previousStatus.get), s"Previous status has invalid value: ${previousStatus.get}")
  }
}
