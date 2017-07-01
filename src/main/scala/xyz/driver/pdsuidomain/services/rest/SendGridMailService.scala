package xyz.driver.pdsuidomain.services.rest

import com.sendgrid._
import xyz.driver.pdsuicommon.logging._
import xyz.driver.pdsuidomain.services.MailService
import xyz.driver.pdsuidomain.services.MailService.Template
import xyz.driver.pdsuidomain.services.rest.SendGridMailService._

import scala.util.control.NonFatal

object SendGridMailService {

  private val ExpectedHttpCode = 202

  case class Settings(provider: String, frontEndUrl: String, apiKey: String, from: String)
}

class SendGridMailService(settings: Settings) extends MailService with PhiLogging {

  def sendTo(email: String, template: Template): Boolean = {
    val to = new Email(email)
    val content = new Content(template.contentType, template.content)
    val mail = new Mail(new Email(settings.from), template.subject, to, content)

    val request = new Request()
    val sendGrid = new SendGrid(settings.apiKey)

    try {
      request.method = Method.POST
      request.endpoint = "mail/send"
      request.body = mail.build()
      val response = sendGrid.api(request)
      if (response.statusCode != ExpectedHttpCode) {
        logger.error(phi"Unexpected response: ${Unsafe(response.statusCode)}, ${Unsafe(response.body.take(100))}")
      }

      response.statusCode == ExpectedHttpCode
    }
    catch {
      case NonFatal(e) =>
        logger.error(phi"Can not send an email: $e")
        false
    }
  }
}
