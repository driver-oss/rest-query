package xyz.driver.restquery.http.parsers

import scala.util.{Failure, Success, Try}

class Dimensions(private val xs: Set[String] = Set.empty) {
  def contains(x: String): Boolean = xs.isEmpty || xs.contains(x)
}

object DimensionsParser {

  @deprecated("play-akka transition", "0")
  def tryParse(query: Map[String, Seq[String]]): Try[Dimensions] =
    tryParse(query.toSeq.flatMap {
      case (key, values) =>
        values.map(value => key -> value)
    })

  def tryParse(query: Seq[(String, String)]): Try[Dimensions] = {
    query.collect { case ("dimensions", value) => value } match {
      case Nil => Success(new Dimensions())

      case x +: Nil =>
        val raw: Set[String] = x.split(",").view.map(_.trim).filter(_.nonEmpty).to[Set]
        Success(new Dimensions(raw))

      case xs =>
        Failure(new IllegalArgumentException(s"Dimensions are specified ${xs.size} times"))
    }
  }
}
