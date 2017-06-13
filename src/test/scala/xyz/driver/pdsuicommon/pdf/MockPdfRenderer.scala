package xyz.driver.pdsuicommon.pdf

import java.nio.file.{Path, Paths}

import xyz.driver.pdsuicommon.logging._

object MockPdfRenderer extends PdfRenderer with PhiLogging {

  private lazy val defaultDocument: Path = {
    val uri = getClass.getResource("/pdf/example.pdf").toURI
    Paths.get(uri)
  }

  override def render(html: String, documentName: String, force: Boolean = false): Path = {
    logger.trace(phi"render(html, documentName=${Unsafe(documentName)})")
    defaultDocument
  }

  override def delete(documentName: String): Unit = {
    logger.trace(phi"delete(${Unsafe(documentName)})")
  }

  override def getPath(documentName: String): Path = defaultDocument

}
