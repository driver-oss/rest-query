package xyz.driver.pdsuicommon.auth

import xyz.driver.entities.users.UserInfo
import xyz.driver.pdsuicommon.logging._
import xyz.driver.pdsuicommon.domain.User

class AuthenticatedRequestContext(val driverUser: UserInfo, override val requestId: RequestId)
    extends AnonymousRequestContext(requestId) {

  val executor: User = new User(driverUser)

  override def equals(that: Any): Boolean = {
    that.getClass == this.getClass && {
      val another = that.asInstanceOf[AuthenticatedRequestContext]
      another.executor == executor && another.requestId == requestId
    }
  }

  override def hashCode(): Int = {
    val initial = 37
    val first   = initial * 17 + executor.hashCode()
    first * 17 + requestId.hashCode()
  }
}

object AuthenticatedRequestContext {

  def apply(driverUser: UserInfo) =
    new AuthenticatedRequestContext(driverUser, RequestId())

  implicit def toPhiString(x: AuthenticatedRequestContext): PhiString = {
    phi"AuthenticatedRequestContext(executor=${x.executor}, requestId=${x.requestId})"
  }
}
