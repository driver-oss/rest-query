package xyz.driver.pdsuidomain.formats.json

import java.time.{LocalDate, LocalDateTime, ZoneId, ZonedDateTime}

import spray.json._
import xyz.driver.pdsuicommon.domain.{LongId, StringId, UuidId}

object common {

  implicit def longIdFormat[T] = new RootJsonFormat[LongId[T]] {
    override def write(id: LongId[T]): JsNumber = JsNumber(id.id)
    override def read(json: JsValue): LongId[T] = json match {
      case JsNumber(value) => LongId(value.toLong)
      case _               => deserializationError(s"Expected number as LongId, but got $json")
    }
  }

  implicit def stringIdFormat[T] = new RootJsonFormat[StringId[T]] {
    override def write(id: StringId[T]): JsString = JsString(id.toString)
    override def read(json: JsValue): StringId[T] = json match {
      case JsString(value) => StringId(value)
      case _               => deserializationError(s"Expected string as StringId, but got $json")
    }
  }

  implicit def uuidIdFormat[T] = new RootJsonFormat[UuidId[T]] {
    override def write(id: UuidId[T]): JsString = JsString(id.toString)
    override def read(json: JsValue): UuidId[T] = json match {
      case JsString(value) => UuidId(value)
      case _               => deserializationError(s"Expected string as UuidId, but got $json")
    }
  }

  implicit def dateTimeFormat = new RootJsonFormat[LocalDateTime] {
    override def write(date: LocalDateTime): JsString = JsString(ZonedDateTime.of(date, ZoneId.of("Z")).toString)
    override def read(json: JsValue): LocalDateTime = json match {
      case JsString(value) => ZonedDateTime.parse(value).toLocalDateTime
      case _               => deserializationError(s"Expected date as LocalDateTime, but got $json")
    }
  }

  implicit def zonedDateTimeFormat = new RootJsonFormat[ZonedDateTime] {
    override def write(date: ZonedDateTime): JsString = JsString(date.toString)
    override def read(json: JsValue): ZonedDateTime = json match {
      case JsString(value) => ZonedDateTime.parse(value)
      case _               => deserializationError(s"Expected date as ZonedDateTime, but got $json")
    }
  }

  implicit def dateFormat = new RootJsonFormat[LocalDate] {
    override def write(date: LocalDate): JsString = JsString(date.toString)
    override def read(json: JsValue): LocalDate = json match {
      case JsString(value) => LocalDate.parse(value)
      case _               => deserializationError(s"Expected date as LocalDate, but got $json")
    }
  }

  implicit val integerFormat: RootJsonFormat[Integer] = new RootJsonFormat[Integer] {
    override def write(obj: Integer): JsNumber = JsNumber(obj.intValue())
    override def read(json: JsValue): Integer = json match {
      case JsNumber(value) => value.toInt
      case _               => deserializationError(s"Expected number as Integer, but got $json")
    }
  }

}
