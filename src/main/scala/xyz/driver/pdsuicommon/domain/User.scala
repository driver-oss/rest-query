package xyz.driver.pdsuicommon.domain

import java.math.BigInteger
import java.security.SecureRandom
import java.time.{Instant, LocalDateTime, ZoneId}

import xyz.driver.pdsuicommon.logging._
import xyz.driver.pdsuicommon.domain.User.Role
import xyz.driver.pdsuicommon.utils.Utils

final case class User(id: StringId[User],
                      email: Email,
                      name: String,
                      roles: Set[Role],
                      latestActivity: Option[LocalDateTime],
                      deleted: Option[LocalDateTime]) {

  def this(driverUser: xyz.driver.entities.users.UserInfo) {
    this(
      id = StringId[xyz.driver.pdsuicommon.domain.User](driverUser.id.value),
      email = Email(driverUser.email.toString),
      name = driverUser.name.toString,
      roles = driverUser.roles.flatMap(User.mapRoles),
      latestActivity =
        driverUser.lastLoginTime.map(t => Instant.ofEpochMilli(t.millis).atZone(ZoneId.of("Z")).toLocalDateTime),
      deleted = Option.empty[LocalDateTime]
    )
  }
}

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
    case object RecordAdmin            extends Role { val bit = 1 << 0  }
    case object RecordCleaner          extends Role { val bit = 1 << 1  }
    case object RecordOrganizer        extends Role { val bit = 1 << 2  }
    case object DocumentExtractor      extends Role { val bit = 1 << 3  }
    case object TrialSummarizer        extends Role { val bit = 1 << 4  }
    case object CriteriaCurator        extends Role { val bit = 1 << 5  }
    case object TrialAdmin             extends Role { val bit = 1 << 6  }
    case object EligibilityVerifier    extends Role { val bit = 1 << 7  }
    case object TreatmentMatchingAdmin extends Role { val bit = 1 << 8  }
    case object RoutesCurator          extends Role { val bit = 1 << 9  }
    case object SystemUser             extends Role { val bit = 1 << 10 }
    case object ResearchOncologist     extends Role { val bit = 1 << 11 }

    val RepRoles = Set[Role](RecordAdmin, RecordCleaner, RecordOrganizer, DocumentExtractor)

    val TcRoles = Set[Role](TrialSummarizer, CriteriaCurator, TrialAdmin)

    val TreatmentMatchingRoles = Set[Role](RoutesCurator, EligibilityVerifier, TreatmentMatchingAdmin)

    val PepRoles = Set[Role](ResearchOncologist)

    val All = RepRoles ++ TcRoles ++ TreatmentMatchingRoles ++ PepRoles + SystemUser

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
    phi"User(id=$id, roles=${Unsafe(roles.map(_.toString).mkString(", "))})"
  }

  // SecureRandom is thread-safe, see the implementation
  private val random = new SecureRandom()

  def createPassword: String = new BigInteger(240, random).toString(32)

  def mapRoles(coreRole: xyz.driver.core.auth.Role): Set[xyz.driver.pdsuicommon.domain.User.Role] = {
    coreRole match {
      case xyz.driver.entities.auth.AdministratorRole =>
        Set(
          xyz.driver.pdsuicommon.domain.User.Role.SystemUser,
          xyz.driver.pdsuicommon.domain.User.Role.RecordAdmin,
          xyz.driver.pdsuicommon.domain.User.Role.TrialAdmin,
          xyz.driver.pdsuicommon.domain.User.Role.TreatmentMatchingAdmin
        )
      case xyz.driver.entities.auth.RecordAdmin =>
        Set(xyz.driver.pdsuicommon.domain.User.Role.RecordAdmin)
      case xyz.driver.entities.auth.RecordCleaner =>
        Set(xyz.driver.pdsuicommon.domain.User.Role.RecordCleaner)
      case xyz.driver.entities.auth.RecordOrganizer =>
        Set(xyz.driver.pdsuicommon.domain.User.Role.RecordOrganizer)
      case xyz.driver.entities.auth.DocumentExtractor =>
        Set(xyz.driver.pdsuicommon.domain.User.Role.DocumentExtractor)
      case xyz.driver.entities.auth.TrialSummarizer =>
        Set(xyz.driver.pdsuicommon.domain.User.Role.TrialSummarizer)
      case xyz.driver.entities.auth.CriteriaCurator =>
        Set(xyz.driver.pdsuicommon.domain.User.Role.CriteriaCurator)
      case xyz.driver.entities.auth.TrialAdmin =>
        Set(xyz.driver.pdsuicommon.domain.User.Role.TrialAdmin)
      case xyz.driver.entities.auth.EligibilityVerifier =>
        Set(xyz.driver.pdsuicommon.domain.User.Role.EligibilityVerifier)
      case xyz.driver.entities.auth.TreatmentMatchingAdmin =>
        Set(xyz.driver.pdsuicommon.domain.User.Role.TreatmentMatchingAdmin)
      case xyz.driver.entities.auth.RoutesCurator =>
        Set(xyz.driver.pdsuicommon.domain.User.Role.RoutesCurator)
      case xyz.driver.entities.auth.ResearchOncologist =>
        Set(xyz.driver.pdsuicommon.domain.User.Role.ResearchOncologist)
      case _ =>
        Set.empty[xyz.driver.pdsuicommon.domain.User.Role]
    }
  }

  def mapRolesToDriver(pdsuiRole: xyz.driver.pdsuicommon.domain.User.Role): Set[xyz.driver.core.auth.Role] = {
    pdsuiRole match {
      case xyz.driver.pdsuicommon.domain.User.Role.SystemUser =>
        Set(xyz.driver.entities.auth.AdministratorRole)
      case xyz.driver.pdsuicommon.domain.User.Role.RecordAdmin =>
        Set(xyz.driver.entities.auth.RecordAdmin)
      case xyz.driver.pdsuicommon.domain.User.Role.RecordCleaner =>
        Set(xyz.driver.entities.auth.RecordCleaner)
      case xyz.driver.pdsuicommon.domain.User.Role.RecordOrganizer =>
        Set(xyz.driver.entities.auth.RecordOrganizer)
      case xyz.driver.pdsuicommon.domain.User.Role.DocumentExtractor =>
        Set(xyz.driver.entities.auth.DocumentExtractor)
      case xyz.driver.pdsuicommon.domain.User.Role.TrialSummarizer =>
        Set(xyz.driver.entities.auth.TrialSummarizer)
      case xyz.driver.pdsuicommon.domain.User.Role.CriteriaCurator =>
        Set(xyz.driver.entities.auth.CriteriaCurator)
      case xyz.driver.pdsuicommon.domain.User.Role.TrialAdmin =>
        Set(xyz.driver.entities.auth.TrialAdmin)
      case xyz.driver.pdsuicommon.domain.User.Role.EligibilityVerifier =>
        Set(xyz.driver.entities.auth.EligibilityVerifier)
      case xyz.driver.pdsuicommon.domain.User.Role.TreatmentMatchingAdmin =>
        Set(xyz.driver.entities.auth.TreatmentMatchingAdmin)
      case xyz.driver.pdsuicommon.domain.User.Role.RoutesCurator =>
        Set(xyz.driver.entities.auth.RoutesCurator)
      case xyz.driver.pdsuicommon.domain.User.Role.ResearchOncologist =>
        Set(xyz.driver.entities.auth.ResearchOncologist)
      case _ =>
        Set.empty[xyz.driver.core.auth.Role]
    }
  }
}
