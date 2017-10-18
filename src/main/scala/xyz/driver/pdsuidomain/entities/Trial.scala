package xyz.driver.pdsuidomain.entities

import java.time.LocalDateTime

import xyz.driver.pdsuicommon.domain.{LongId, StringId, User, UuidId}
import xyz.driver.pdsuicommon.logging._
import xyz.driver.pdsuicommon.utils.Utils
import xyz.driver.pdsuidomain.entities.Trial.Status

import scalaz.syntax.equal._
import scalaz.Scalaz.stringInstance

sealed trait StudyDesign {
  val id: LongId[StudyDesign]
  val name: String
}

object StudyDesign {

  final case object Randomized extends StudyDesign {
    val id: LongId[StudyDesign] = LongId[StudyDesign](1)
    val name: String            = "Randomized"
  }

  final case object NonRandomized extends StudyDesign {
    val id: LongId[StudyDesign] = LongId[StudyDesign](2)
    val name: String            = "Non-randomized"
  }

  final case object SingleGroupAssignment extends StudyDesign {
    val id: LongId[StudyDesign] = LongId[StudyDesign](3)
    val name: String            = "Single-group assignment"
  }

  val All = Seq[StudyDesign](
    Randomized,
    NonRandomized,
    SingleGroupAssignment
  )

  def fromString(txt: String): Option[StudyDesign] = {
    All.find(_.name === txt)
  }

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
