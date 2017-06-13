package xyz.driver.pdsuidomain.storage

import com.google.cloud.storage.StorageOptions
import com.typesafe.scalalogging.StrictLogging
import xyz.driver.pdsuidomain.entities.MedicalRecord.PdfSource

import scala.concurrent.{ExecutionContext, Future, blocking}

object MedicalRecordDocumentStorage extends StrictLogging {
  private val storage = StorageOptions.getDefaultInstance.getService

  def fetchPdf(bucket: String, path: String)
              (implicit ec: ExecutionContext): Future[PdfSource] = {
    logger.trace(s"fetchPdf(bucket=$bucket, path=$path)")
    Future {
      blocking {
        Option(storage.get(bucket, path)) match {
          case Some(blob) =>
            PdfSource.Channel(() => blob.reader())
          case None =>
            logger.error(s"Failed to find the pdf file $path in bucket: $bucket")
            PdfSource.Empty
        }
      }
    }
  }
}
