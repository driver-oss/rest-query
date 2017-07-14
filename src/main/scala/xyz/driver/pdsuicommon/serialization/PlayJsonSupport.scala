package xyz.driver.pdsuicommon.serialization

import akka.http.scaladsl.server.{RejectionError, ValidationRejection}
import akka.http.scaladsl.unmarshalling.Unmarshaller
import play.api.libs.json.{Reads, Writes}
import play.api.libs.json.Json
import akka.http.scaladsl.marshalling.ToEntityMarshaller
import akka.http.scaladsl.unmarshalling.FromEntityUnmarshaller
import akka.http.scaladsl.model.MediaTypes.`application/json`

trait PlayJsonSupport {
  import akka.http.scaladsl.marshalling.Marshaller

  implicit def playJsonUnmarshaller[A: Reads]: FromEntityUnmarshaller[A] = {
    val reads = implicitly[Reads[A]]
    Unmarshaller.stringUnmarshaller
      .forContentTypes(`application/json`)
      .map(Json.parse)
      .map(reads.reads)
      .map(_.recoverTotal { error =>
        throw RejectionError(ValidationRejection(s"Error reading JSON response as ${reads}."))
      })
  }

  implicit def playJsonMarshaller[A: Writes]: ToEntityMarshaller[A] = {
    Marshaller
      .stringMarshaller(`application/json`)
      .compose(Json.prettyPrint)
      .compose(implicitly[Writes[A]].writes)
  }

}

object PlayJsonSupport extends PlayJsonSupport
