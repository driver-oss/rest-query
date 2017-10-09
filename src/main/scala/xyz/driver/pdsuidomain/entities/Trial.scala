package xyz.driver.pdsuidomain.entities

import java.time.LocalDateTime

import xyz.driver.entities.patient.CancerType
import xyz.driver.pdsuicommon.domain.{LongId, StringId, User, UuidId}
import xyz.driver.pdsuicommon.logging._
import xyz.driver.pdsuicommon.utils.Utils
import xyz.driver.pdsuidomain.entities.Trial.Status

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

  implicit def toPhiString(x: Trial): PhiString = {
    import x._
    phi"Trial(id=$id, externalId=$externalId, status=$status, previousStatus=$previousStatus, " +
      phi"lastActiveUserId=$lastActiveUserId, assignee=$assignee, previousAssignee=$previousAssignee, "
  }

  final case class Locations(locations: List[String])
}

final case class Trial(id: StringId[Trial],
                       externalId: UuidId[Trial],
                       status: Status,
                       assignee: Option[StringId[User]],
                       previousStatus: Option[Status],
                       previousAssignee: Option[StringId[User]],
                       lastActiveUserId: Option[StringId[User]],
                       lastUpdate: LocalDateTime,
                       disease: CancerType,
                       phase: String,
                       hypothesisId: Option[UuidId[Hypothesis]],
                       studyDesignId: Option[LongId[StudyDesign]],
                       originalStudyDesign: Option[String],
                       isPartner: Boolean,
                       overview: Option[String],
                       overviewTemplate: String,
                       isUpdated: Boolean,
                       title: String,
                       originalTitle: String) {
  import Trial.Status._

  if (previousStatus.nonEmpty) {
    assert(AllPrevious.contains(previousStatus.get), s"Previous status has invalid value: ${previousStatus.get}")
  }
}
