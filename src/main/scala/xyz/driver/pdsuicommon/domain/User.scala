package xyz.driver.pdsuicommon.domain

import java.time.LocalDateTime

import xyz.driver.pdsuicommon.logging._
import xyz.driver.pdsuicommon.domain.User.Role
import xyz.driver.pdsuicommon.utils.Utils

case class User(id: LongId[User],
                email: Email,
                name: String,
                role: Role,
                passwordHash: PasswordHash,
                latestActivity: Option[LocalDateTime],
                deleted: Option[LocalDateTime])

object User {

  sealed trait Role extends Product with Serializable {

    /**
      * Bit representation of this role
      */
    def bit: Int

    def is(that: Role): Boolean = this == that

    def oneOf(roles: Role*): Boolean = roles.contains(this)

    def oneOf(roles: Set[Role]): Boolean = roles.contains(this)
  }

  object Role extends PhiLogging {
    case object RecordAdmin extends Role {val bit = 1 << 0}
    case object RecordCleaner extends Role {val bit = 1 << 1}
    case object RecordOrganizer extends Role {val bit = 1 << 2}
    case object DocumentExtractor extends Role {val bit = 1 << 3}
    case object TrialSummarizer extends Role {val bit = 1 << 4}
    case object CriteriaCurator extends Role {val bit = 1 << 5}
    case object TrialAdmin extends Role {val bit = 1 << 6}
    case object EligibilityVerifier extends Role{val bit = 1 << 7}
    case object TreatmentMatchingAdmin extends Role{val bit = 1 << 8}
    case object RoutesCurator extends Role{val bit = 1 << 9}

    val RepRoles = Set[Role](RecordAdmin, RecordCleaner, RecordOrganizer, DocumentExtractor)

    val TcRoles = Set[Role](TrialSummarizer, CriteriaCurator, TrialAdmin)

    val TreatmentMatchingRoles = Set[Role](RoutesCurator, EligibilityVerifier, TreatmentMatchingAdmin)

    val All = RepRoles ++ TcRoles ++ TreatmentMatchingRoles

    def apply(bitMask: Int): Role = {
      def extractRole(role: Role): Set[Role] =
        if ((bitMask & role.bit) != 0) Set(role) else Set.empty[Role]

      val roles = All.flatMap(extractRole)
      roles.size match {
        case 1 => roles.head
        case _ =>
          logger.error(phi"Can't convert a bit mask ${Unsafe(bitMask)} to any role")
          throw new IllegalArgumentException()
      }
    }

    implicit def toPhiString(x: Role): PhiString = Unsafe(Utils.getClassSimpleName(x.getClass))
  }

  implicit def toPhiString(x: User): PhiString = {
    import x._
    phi"User(id=$id, role=$role)"
  }

}
