package xyz.driver.pdsuicommon.error

import xyz.driver.pdsuicommon.logging.{PhiString, Unsafe}
import xyz.driver.pdsuicommon.utils.Utils

trait DomainError {

  protected def userMessage: String

  def getMessage: String = userMessage

}

object DomainError {

  // 404 error
  trait NotFoundError extends DomainError

  // 401 error
  trait AuthenticationError extends DomainError

  // 403 error
  trait AuthorizationError extends DomainError

  implicit def toPhiString(x: DomainError): PhiString = {
    // userMessage possibly can contain a personal information,
    // so we should prevent it to be printed in logs.
    Unsafe(Utils.getClassSimpleName(x.getClass))
  }
}
