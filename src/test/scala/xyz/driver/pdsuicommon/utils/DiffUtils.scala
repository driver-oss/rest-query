package xyz.driver.pdsuicommon.utils

import java.net.URI
import java.time.{LocalDate, LocalDateTime}

import ai.x.diff._
import org.scalatest.Assertions
import xyz.driver.pdsuidomain.entities.{Document, ExtractedData, MedicalRecord}

import scala.io.AnsiColor

trait DiffUtils { this: Assertions =>

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

  implicit def metaDiffShow: DiffShow[MedicalRecord.Meta] = new DiffShow[MedicalRecord.Meta] {
    def show(x: MedicalRecord.Meta): String = s"MedicalRecord.Meta($x)"
    def diff(left: MedicalRecord.Meta, right: MedicalRecord.Meta): Comparison = {
      if (left.equals(right)) Identical(show(left))
      else Different(showChange(left, right))
    }
  }

  implicit def extractedDataMetaDiffShow: DiffShow[ExtractedData.Meta] = new DiffShow[ExtractedData.Meta] {
    def show(x: ExtractedData.Meta): String = s"ExtractedData.Meta($x)"
    def diff(left: ExtractedData.Meta, right: ExtractedData.Meta): Comparison = {
      if (left.equals(right)) Identical(show(left))
      else Different(showChange(left, right))
    }
  }

  implicit def documentDiffShow: DiffShow[Document] = new DiffShow[Document] {
    def show(x: Document): String = s"Document($x)"

    def diff(left: Document, right: Document): Comparison = {
      if (left == right) Identical(show(left))
      else Different(showChange(left, right))
    }
  }

}
