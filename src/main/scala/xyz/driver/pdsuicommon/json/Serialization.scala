package xyz.driver.pdsuicommon.json

import java.net.URI

import play.api.libs.functional.syntax._
import play.api.libs.json._
import xyz.driver.pdsuicommon.domain._

object Serialization {

  // @TODO Test and check all items in an array
  private def seqJsonReads[T](implicit argFormat: Reads[T]): Reads[Seq[T]] = Reads {
    case JsArray(xs) => JsSuccess(xs.map { x => argFormat.reads(x).get })
    case x => JsError(s"Expected JsArray, but got $x")
  }

  private def seqJsonWrites[T](implicit argFormat: Writes[T]): Writes[Seq[T]] = Writes { xs =>
    JsArray(xs.map(argFormat.writes))
  }

  implicit def seqJsonFormat[T](implicit f: Format[T]): Format[Seq[T]] = Format(seqJsonReads[T], seqJsonWrites[T])

  private val uriJsonReads: Reads[URI] = Reads.StringReads.map(URI.create)
  private val uriJsonWrites: Writes[URI] = Writes(uri => JsString(uri.toString))
  implicit val uriJsonFormat: Format[URI] = Format(uriJsonReads, uriJsonWrites)

  private def uuidIdJsonReads[T]: Reads[UuidId[T]] = Reads.uuidReads.map(x => UuidId[T](x))
  private def uuidIdJsonWrites[T]: Writes[UuidId[T]] = Writes.UuidWrites.contramap(_.id)
  implicit def uuidIdJsonFormat[T]: Format[UuidId[T]] = Format(uuidIdJsonReads, uuidIdJsonWrites)

  private def longIdJsonReads[T]: Reads[LongId[T]] = Reads.LongReads.map(x => LongId[T](x))
  private def longIdJsonWrites[T]: Writes[LongId[T]] = Writes.LongWrites.contramap(_.id)
  implicit def longIdJsonFormat[T]: Format[LongId[T]] = Format(longIdJsonReads, longIdJsonWrites)

  private val emailJsonReads: Reads[Email] = Reads.email.map(Email.apply)
  private val emailJsonWrites: Writes[Email] = Writes(email => JsString(email.value))
  implicit val emailJsonFormat: Format[Email] = Format(emailJsonReads, emailJsonWrites)

  private val passwordHashJsonReads: Reads[PasswordHash] = Reads.StringReads.map(hash => PasswordHash(hash.getBytes("UTF-8")))
  private val passwordHashJsonWrites: Writes[PasswordHash] = Writes(passwordHash => JsString(passwordHash.value.toString))
  implicit val passwordHashJsonFormat: Format[PasswordHash] = Format(passwordHashJsonReads, passwordHashJsonWrites)
}
