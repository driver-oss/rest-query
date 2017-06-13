package xyz.driver.common.logging

import org.slf4j.LoggerFactory

trait PhiLogging extends Implicits {

  protected val logger: PhiLogger = new DefaultPhiLogger(LoggerFactory.getLogger(getClass.getName))

  /**
    * Logs the failMessage on an error level, if isSuccessful is false.
    * @return isSuccessful
    */
  protected def loggedError(isSuccessful: Boolean, failMessage: PhiString): Boolean = {
    if (!isSuccessful) {
      logger.error(failMessage)
    }
    isSuccessful
  }

}
