package xyz.driver.pdsuicommon.error

import play.api.libs.functional.syntax._
import play.api.libs.json.{Format, Reads, Writes}

@SuppressWarnings(Array("org.wartremover.warts.Enumeration"))
object ErrorCode extends Enumeration {

  type ErrorCode = Value
  val Unspecified = Value(1)

  private val fromJsonReads: Reads[ErrorCode] = Reads.of[Int].map(ErrorCode.apply)
  private val toJsonWrites: Writes[ErrorCode] = Writes.of[Int].contramap(_.id)

  implicit val jsonFormat: Format[ErrorCode] = Format(fromJsonReads, toJsonWrites)

}
