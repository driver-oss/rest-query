package xyz.driver.pdsuicommon.auth

import xyz.driver.pdsuicommon.logging._
import xyz.driver.pdsuicommon.domain.User

class AuthenticatedRequestContext(val executor: User, override val requestId: RequestId, val authToken: String = "")
    extends AnonymousRequestContext(requestId) {

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

  def apply(executor: User, authToken: String) = new AuthenticatedRequestContext(executor, RequestId(), authToken)

  implicit def toPhiString(x: AuthenticatedRequestContext): PhiString = {
    phi"AuthenticatedRequestContext(executor=${x.executor}, requestId=${x.requestId})"
  }

}
