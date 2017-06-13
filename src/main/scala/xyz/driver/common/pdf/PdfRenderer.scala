package xyz.driver.common.pdf

import java.nio.file.Path

trait PdfRenderer {

  def render(html: String, documentName: String, force: Boolean = false): Path

  def delete(documentName: String): Unit

  def getPath(fileName: String): Path

}
