package xyz.driver.pdsuicommon.logging

import java.io.File
import java.net.{URI, URL}
import java.nio.file.Path
import java.time.LocalDateTime
import java.util.UUID

import scala.concurrent.duration.Duration

trait Implicits {

  // DO NOT ADD!
  // phi"$fullName" is easier to write, than phi"${Unsafe(fullName)}"
  // If you wrote the second version, it means that you know, what you doing.
  // implicit def toPhiString(s: String): PhiString = Unsafe(s)

  implicit def toPhiStringContext(sc: StringContext): PhiStringContext = new PhiStringContext(sc)

  implicit def booleanToPhiString(x: Boolean): PhiString = Unsafe(x.toString)

  implicit def uriToPhiString(x: URI): PhiString = Unsafe(x.toString)

  implicit def urlToPhiString(x: URL): PhiString = Unsafe(x.toString)

  implicit def pathToPhiString(x: Path): PhiString = Unsafe(x.toString)

  implicit def fileToPhiString(x: File): PhiString = Unsafe(x.toString)

  implicit def localDateTimeToPhiString(x: LocalDateTime): PhiString = Unsafe(x.toString)

  implicit def durationToPhiString(x: Duration): PhiString = Unsafe(x.toString)

  implicit def uuidToPhiString(x: UUID): PhiString = Unsafe(x.toString)

  implicit def tuple2ToPhiString[T1, T2](x: (T1, T2))(implicit inner1: T1 => PhiString,
                                                      inner2: T2 => PhiString): PhiString = x match {
    case (a, b) => phi"($a, $b)"
  }

  implicit def tuple3ToPhiString[T1, T2, T3](x: (T1, T2, T3))(implicit inner1: T1 => PhiString,
                                                              inner2: T2 => PhiString,
                                                              inner3: T3 => PhiString): PhiString = x match {
    case (a, b, c) => phi"($a, $b, $c)"
  }

  implicit def optionToPhiString[T](opt: Option[T])(implicit inner: T => PhiString): PhiString = opt match {
    case None    => phi"None"
    case Some(x) => phi"Some($x)"
  }

  implicit def iterableToPhiString[T](xs: Iterable[T])(implicit inner: T => PhiString): PhiString = {
    Unsafe(xs.map(inner(_).text).mkString("Col(", ", ", ")"))
  }

  implicit def throwableToPhiString(x: Throwable): PhiString = {
    Unsafe(Option(x.getMessage).getOrElse(x.getClass.getName))
  }

}
