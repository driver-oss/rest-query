package xyz.driver.pdsuidomain.services.fake

import xyz.driver.pdsuicommon.logging._
import xyz.driver.pdsuidomain.services.MailService
import xyz.driver.pdsuidomain.services.MailService.Template

object StubMailService extends MailService with PhiLogging {

  override def sendTo(email: String, template: Template): Boolean = {
    logger.debug(phi"sendTo(email=${Unsafe(email)}")
    true
  }
}
