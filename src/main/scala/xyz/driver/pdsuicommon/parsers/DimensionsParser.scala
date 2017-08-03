package xyz.driver.pdsuicommon.parsers

//import play.api.libs.json._
//import xyz.driver.pdsuicommon.utils.WritesUtils

import scala.util.{Failure, Success, Try}

class Dimensions(private val xs: Set[String] = Set.empty) {
  def contains(x: String): Boolean = xs.isEmpty || xs.contains(x)
}

object DimensionsParser {

  /*
  private class DimensionsWrapper[T](dimensions: Dimensions)(implicit orig: Writes[T]) extends Writes[T] {
    private val filteredWrites         = WritesUtils.filterKeys[T](dimensions.contains)
    override def writes(o: T): JsValue = filteredWrites.writes(o)
  }
   */

  def tryParse(query: Seq[(String, String)]): Try[Dimensions] = {
    query.collect{ case ("dimensions", value) => value }  match {
      case Nil => Success(new Dimensions())

      case x +: Nil =>
        val raw: Set[String] = x.split(",").view.map(_.trim).filter(_.nonEmpty).to[Set]
        Success(new Dimensions(raw))

      case xs =>
        Failure(new IllegalArgumentException(s"Dimensions are specified ${xs.size} times"))
    }
  }
}