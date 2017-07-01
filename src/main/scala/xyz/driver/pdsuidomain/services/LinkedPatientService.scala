package xyz.driver.pdsuidomain.services

import xyz.driver.pdsuicommon.domain._
import xyz.driver.pdsuicommon.error.DomainError
import xyz.driver.pdsuicommon.logging._
import xyz.driver.pdsuidomain.entities.{LinkedPatient, Patient, Trial}

import scala.concurrent.Future

object LinkedPatientService {

  trait DefaultTrialNotFoundError {
    def userMessage: String = "Trial not found"
  }

  trait DefaultPatientNotFoundError {
    def userMessage: String = "Patient not found"
  }

  final case class RichLinkedPatient(email: Email, name: String, patientId: UuidId[Patient], trialId: StringId[Trial]) {
    def toLinkedPatient(user: User) = LinkedPatient(
      userId = user.id,
      patientId = patientId,
      trialId = trialId
    )
  }

  object RichLinkedPatient {
    implicit def toPhiString(x: RichLinkedPatient): PhiString = {
      import x._
      phi"RichLinkedPatient(email=${Unsafe(email)}, patientId=$patientId, trialId=$trialId)"
    }
  }

  sealed trait CreateReply
  object CreateReply {
    type Error = CreateReply with DomainError

    /**
      * @param createdUser None if a user was created before
      */
    final case class Created(x: RichLinkedPatient, createdUser: Option[User]) extends CreateReply

    case object PatientNotFoundError
        extends CreateReply with DefaultPatientNotFoundError with DomainError.NotFoundError

    case object TrialNotFoundError extends CreateReply with DefaultPatientNotFoundError with DomainError.NotFoundError

    final case class CommonError(userMessage: String) extends CreateReply with DomainError
  }
}

trait LinkedPatientService {

  import LinkedPatientService._

  def create(entity: RichLinkedPatient): Future[CreateReply]
}
