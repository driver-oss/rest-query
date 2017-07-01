package xyz.driver.pdsuidomain.entities

import java.time.{LocalDate, LocalDateTime}

import xyz.driver.pdsuicommon.domain.{LongId, User, UuidId}
import xyz.driver.pdsuicommon.logging._
import xyz.driver.pdsuicommon.utils.Utils

object Patient {

  // Product with Serizalizable fixes issue:
  // Set(New, Verified) has type Set[Status with Product with Serializable]
  sealed trait Status extends Product with Serializable {
    def oneOf(xs: Status*): Boolean = xs.contains(this)

    def oneOf(xs: Set[Status]): Boolean = xs.contains(this)
  }

  object Status {
    case object New      extends Status
    case object Verified extends Status
    case object Reviewed extends Status
    case object Curated  extends Status
    case object Flagged  extends Status
    case object Done     extends Status

    val AllPrevious = Set[Status](
      New,
      Verified,
      Reviewed,
      Curated
    )

    val All = Set[Status](
      New,
      Verified,
      Reviewed,
      Curated,
      Flagged,
      Done
    )

    implicit def toPhiString(x: Status): PhiString = Unsafe(Utils.getClassSimpleName(x.getClass))
  }

  implicit def toPhiString(x: Patient): PhiString = {
    import x._
    phi"Patient(id=$id, status=$status, previousStatus=$previousStatus, lastActiveUserId=$lastActiveUserId" +
      phi"assignee=$assignee, previousAssignee=$previousAssignee)"
  }
}

final case class Patient(id: UuidId[Patient],
                         status: Patient.Status,
                         name: String,
                         dob: LocalDate,
                         assignee: Option[LongId[User]],
                         previousStatus: Option[Patient.Status],
                         previousAssignee: Option[LongId[User]],
                         lastActiveUserId: Option[LongId[User]],
                         isUpdateRequired: Boolean,
                         condition: String,
                         orderId: PatientOrderId,
                         lastUpdate: LocalDateTime) {

  import Patient.Status._

  if (previousStatus.nonEmpty) {
    assert(AllPrevious.contains(previousStatus.get), s"Previous status has invalid value: ${previousStatus.get}")
  }
}
