package xyz.driver.pdsuicommon.error

import xyz.driver.core.json.EnumJsonFormat

@SuppressWarnings(Array("org.wartremover.warts.Enumeration"))
object ErrorCode extends Enumeration {

  type ErrorCode = Value
  val Unspecified = Value(1)

  implicit val jsonFormat = new EnumJsonFormat[ErrorCode](
    "200" -> ErrorCode.Value(200),
    "400" -> ErrorCode.Value(400),
    "401" -> ErrorCode.Value(401),
    "403" -> ErrorCode.Value(403),
    "404" -> ErrorCode.Value(404),
    "500" -> ErrorCode.Value(500)
  )

}
