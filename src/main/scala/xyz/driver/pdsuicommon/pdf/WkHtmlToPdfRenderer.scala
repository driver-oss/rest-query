package xyz.driver.pdsuicommon.pdf

import java.io.IOException
import java.nio.file._

import io.github.cloudify.scala.spdf._
import xyz.driver.pdsuicommon.logging._
import xyz.driver.pdsuicommon.pdf.WkHtmlToPdfRenderer.Settings

object WkHtmlToPdfRenderer {

  final case class Settings(downloadsDir: String) {

    lazy val downloadsPath: Path = getPathFrom(downloadsDir)

    private def getPathFrom(x: String): Path = {
      val dirPath = if (x.startsWith("/")) Paths.get(x)
      else {
        val workingDir = Paths.get(".")
        workingDir.resolve(x)
      }

      dirPath.toAbsolutePath.normalize()
    }
  }
}

class WkHtmlToPdfRenderer(settings: Settings) extends PdfRenderer with PhiLogging {

  private val pdf = Pdf(new PdfConfig {
    disableJavascript := true
    disableExternalLinks := true
    disableInternalLinks := true
    printMediaType := Some(true)
    orientation := Portrait
    pageSize := "A4"
    lowQuality := true
  })

  override def render(html: String, documentName: String, force: Boolean = false): Path = {
    checkedCreate(html, documentName, force)
  }

  override def delete(documentName: String): Unit = {
    logger.trace(phi"delete(${Unsafe(documentName)})")

    val file = getPath(documentName)
    logger.debug(phi"File: $file")
    if (Files.deleteIfExists(file)) {
      logger.info(phi"Deleted")
    } else {
      logger.warn(phi"Doesn't exist")
    }
  }

  override def getPath(documentName: String): Path = {
    settings.downloadsPath.resolve(s"$documentName.pdf").toAbsolutePath
  }

  protected def checkedCreate[SourceT: SourceDocumentLike](src: SourceT, fileName: String, force: Boolean): Path = {
    logger.trace(phi"checkedCreate(fileName=${Unsafe(fileName)}, force=$force)")

    val dest = getPath(fileName)
    logger.debug(phi"Destination file: $dest")

    if (force || !dest.toFile.exists()) {
      logger.trace(phi"Force refresh the file")
      val newDocPath = forceCreate(src, dest)
      logger.info(phi"Updated")
      newDocPath
    } else if (dest.toFile.exists()) {
      logger.trace(phi"Already exists")
      dest
    } else {
      logger.trace(phi"The file does not exist")
      val newDocPath = forceCreate(src, dest)
      logger.info(phi"Created")
      newDocPath
    }
  }

  protected def forceCreate[SourceT: SourceDocumentLike](src: SourceT, dest: Path): Path = {
    logger.trace(phi"forceCreate[${Unsafe(src.getClass.getName)}](dest=$dest)")

    val destTemp = Files.createTempFile("driver", ".pdf")
    val destTempFile = destTemp.toFile

    Files.createDirectories(dest.getParent)

    val retCode = pdf.run(src, destTempFile)
    lazy val pdfSize = destTempFile.length()
    if (retCode != 0) {
      // Try to google "wkhtmltopdf returns {retCode}"
      throw new IOException(s"Can create the document, the return code is $retCode")
    } else if (pdfSize == 0) {
      // Anything could happen, e.g. https://github.com/wkhtmltopdf/wkhtmltopdf/issues/2540
      throw new IOException("The pdf is empty")
    } else {
      logger.debug(phi"Size: ${Unsafe(pdfSize)}B")
      Files.move(destTemp, dest, StandardCopyOption.REPLACE_EXISTING)
      dest
    }
  }
}
