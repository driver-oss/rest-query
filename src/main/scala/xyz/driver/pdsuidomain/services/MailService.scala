package xyz.driver.pdsuidomain.services

import java.io.{InputStream, StringReader, StringWriter}

import xyz.driver.pdsuidomain.services.MailService.Template
import com.github.mustachejava.DefaultMustacheFactory
import com.twitter.mustache.ScalaObjectHandler

import scala.io.Source

object MailService {

  trait Template {
    val subject: String
    def parameters: Map[String, Any]
    def filename: String
    val contentType: String = "text/html"

    protected val factory = new DefaultMustacheFactory()
    factory.setObjectHandler(new ScalaObjectHandler)

    protected def inputStream: InputStream = getClass.getClassLoader.getResourceAsStream(filename)
    protected def templateContent: String  = Source.fromInputStream(inputStream).getLines().mkString

    def content: String = {
      val template = factory.compile(new StringReader(templateContent), filename)
      val writer   = new StringWriter
      template
        .execute(writer, parameters)
        .close()
      writer.toString
    }
  }
}

trait MailService {

  def sendTo(email: String, template: Template): Boolean
}
