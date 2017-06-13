package xyz.driver.pdsuicommon.utils

import java.net.URI
import java.time.{LocalDate, LocalDateTime}

import ai.x.diff._
import org.scalatest.Assertions
import xyz.driver.pdsuicommon.domain.PasswordHash

import scala.io.AnsiColor

trait DiffUtils {

  this: Assertions =>

  def assertIdentical[T: DiffShow](left: T, right: T): Unit = {
    val diff = DiffShow.diff(left, right)
    assert(diff.isIdentical, s"\n${AnsiColor.RESET}$diff") // reset red color
  }

  implicit def localTimeDiffShow: DiffShow[LocalDateTime] = new DiffShow[LocalDateTime] {
    def show(x: LocalDateTime): String = s"LocalTime($x)"
    def diff(left: LocalDateTime, right: LocalDateTime): Comparison = {
      if (left.isEqual(right)) Identical(show(left))
      else Different(showChange(left, right))
    }
  }

  implicit def localDateDiffShow: DiffShow[LocalDate] = new DiffShow[LocalDate] {
    def show(x: LocalDate): String = s"LocalDate($x)"
    def diff(left: LocalDate, right: LocalDate): Comparison = {
      if (left.isEqual(right)) Identical(show(left))
      else Different(showChange(left, right))
    }
  }

  implicit def urlDiffShow: DiffShow[URI] = new DiffShow[URI] {
    def show(x: URI): String = s"URI($x)"
    def diff(left: URI, right: URI): Comparison = {
      if (left.equals(right)) Identical(show(left))
      else Different(showChange(left, right))
    }
  }

  implicit def passwordHashDiffShow: DiffShow[PasswordHash] = new DiffShow[PasswordHash] {
    def show(x: PasswordHash): String = s"PasswordHash($x)"
    def diff(left: PasswordHash, right: PasswordHash): Comparison = {
      if (left.equals(right)) Identical(show(left))
      else Different(showChange(left, right))
    }
  }
}
