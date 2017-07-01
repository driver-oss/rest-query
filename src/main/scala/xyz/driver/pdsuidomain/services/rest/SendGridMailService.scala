package xyz.driver.pdsuidomain.services.rest

import com.sendgrid._
import xyz.driver.pdsuicommon.logging._
import xyz.driver.pdsuidomain.services.MailService
import xyz.driver.pdsuidomain.services.MailService.Template

import scala.util.control.NonFatal

class SendGridMailService(apiKey: String, from: String) extends MailService with PhiLogging {
  private val ExpectedHttpCode = 202

  def sendTo(email: String, template: Template): Boolean = {
    val to      = new Email(email)
    val content = new Content(template.contentType, template.content)
    val mail    = new Mail(new Email(from), template.subject, to, content)

    val request  = new Request()
    val sendGrid = new SendGrid(apiKey)

    try {
      request.method = Method.POST
      request.endpoint = "mail/send"
      request.body = mail.build()
      val response = sendGrid.api(request)
      if (response.statusCode != ExpectedHttpCode) {
        logger.error(phi"Unexpected response: ${Unsafe(response.statusCode)}, ${Unsafe(response.body.take(100))}")
      }

      response.statusCode == ExpectedHttpCode
    } catch {
      case NonFatal(e) =>
        logger.error(phi"Can not send an email: $e")
        false
    }
  }
}
