package xyz.driver.restquery.http.parsers

import java.util.UUID

import fastparse.all._
import fastparse.core.Parsed
import xyz.driver.restquery.domain.{SearchFilterBinaryOperation, SearchFilterExpr, SearchFilterNAryOperation}
import xyz.driver.restquery.utils.Utils
import xyz.driver.restquery.utils.Utils._

import scala.util.Try

@SuppressWarnings(Array("org.wartremover.warts.Product", "org.wartremover.warts.Serializable"))
object SearchFilterParser {

  private object BinaryAtomFromTuple {
    def unapply(input: (SearchFilterExpr.Dimension, (String, Any))): Option[SearchFilterExpr.Atom.Binary] = {
      val (dimensionName, (strOperation, value)) = input
      val updatedValue                           = trimIfString(value)

      parseOperation(strOperation.toLowerCase).map { op =>
        SearchFilterExpr.Atom.Binary(dimensionName, op, updatedValue.asInstanceOf[AnyRef])
      }
    }
  }

  private object NAryAtomFromTuple {
    // Compiler warning: unchecked since it is eliminated by erasure, if we user Seq[String]
    def unapply(input: (SearchFilterExpr.Dimension, (String, Seq[_]))): Option[SearchFilterExpr.Atom.NAry] = {
      val (dimensionName, (strOperation, xs)) = input
      val updatedValues                       = xs.map(trimIfString)

      if (strOperation.toLowerCase == "in") {
        Some(
          SearchFilterExpr.Atom
            .NAry(dimensionName, SearchFilterNAryOperation.In, updatedValues.map(_.asInstanceOf[AnyRef])))
      } else {
        None
      }
    }
  }

  private def trimIfString(value: Any) =
    value match {
      case s: String => Utils.safeTrim(s)
      case a         => a
    }

  private val operationsMapping = {
    import xyz.driver.restquery.domain.SearchFilterBinaryOperation._

    Map[String, SearchFilterBinaryOperation](
      "eq"    -> Eq,
      "noteq" -> NotEq,
      "like"  -> Like,
      "gt"    -> Gt,
      "gteq"  -> GtEq,
      "lt"    -> Lt,
      "lteq"  -> LtEq
    )
  }

  private def parseOperation(x: String): Option[SearchFilterBinaryOperation] = operationsMapping.get(x)

  private val whitespaceParser = P(CharPred(Utils.isSafeWhitespace))

  val dimensionParser: Parser[SearchFilterExpr.Dimension] = {
    val identParser = P(
      CharPred(c => c.isLetterOrDigit)
        .rep(min = 1)).!.map(s => SearchFilterExpr.Dimension(None, toSnakeCase(s)))
    val pathParser = P(identParser.! ~ "." ~ identParser.!) map {
      case (left, right) =>
        SearchFilterExpr.Dimension(Some(toSnakeCase(left)), toSnakeCase(right))
    }
    P(pathParser | identParser)
  }

  private val commonOperatorParser: Parser[String] = {
    P(IgnoreCase("eq") | IgnoreCase("like") | IgnoreCase("noteq")).!
  }

  private val numericOperatorParser: Parser[String] = {
    P(IgnoreCase("eq") | IgnoreCase("noteq") | ((IgnoreCase("gt") | IgnoreCase("lt")) ~ IgnoreCase("eq").?)).!
  }

  private val naryOperatorParser: Parser[String] = P(IgnoreCase("in")).!

  private val isPositiveParser: Parser[Boolean] = P(CharIn("-+").!.?).map {
    case Some("-") => false
    case _         => true
  }

  private val digitsParser: Parser[String] = P(CharIn('0' to '9').rep(min = 1).!) // Exclude Unicode "digits"

  private val numberParser: Parser[String] = P(isPositiveParser ~ digitsParser.! ~ ("." ~ digitsParser).!.?).map {
    case (false, intPart, Some(fracPart)) => s"-$intPart.${fracPart.tail}"
    case (false, intPart, None)           => s"-$intPart"
    case (_, intPart, Some(fracPart))     => s"$intPart.${fracPart.tail}"
    case (_, intPart, None)               => s"$intPart"
  }

  private val nAryValueParser: Parser[String] = P(CharPred(_ != ',').rep(min = 1).!)

  private val longParser: Parser[Long] = P(CharIn('0' to '9').rep(min = 1).!.map(_.toLong))

  private val booleanParser: Parser[Boolean] =
    P((IgnoreCase("true") | IgnoreCase("false")).!.map(_.toBoolean))

  private val hexDigit: Parser[String] = P((CharIn('a' to 'f') | CharIn('A' to 'F') | CharIn('0' to '9')).!)

  private val uuidParser: Parser[UUID] =
    P(
      hexDigit.rep(8).! ~ "-" ~ hexDigit.rep(4).! ~ "-" ~ hexDigit.rep(4).! ~ "-" ~ hexDigit.rep(4).! ~ "-" ~ hexDigit
        .rep(12)
        .!).map {
      case (group1, group2, group3, group4, group5) => UUID.fromString(s"$group1-$group2-$group3-$group4-$group5")
    }

  private val binaryAtomParser: Parser[SearchFilterExpr.Atom.Binary] = P(
    dimensionParser ~ whitespaceParser ~
      ((numericOperatorParser.! ~ whitespaceParser ~ (longParser | numberParser.!) ~ End) |
        (commonOperatorParser.! ~ whitespaceParser ~ (uuidParser | booleanParser | AnyChar.rep(min = 1).!) ~ End))
  ).map {
    case BinaryAtomFromTuple(atom) => atom
  }

  private val nAryAtomParser: Parser[SearchFilterExpr.Atom.NAry] = P(
    dimensionParser ~ whitespaceParser ~ (
      naryOperatorParser ~ whitespaceParser ~
        ((longParser.rep(min = 1, sep = ",") ~ End) | (booleanParser.rep(min = 1, sep = ",") ~ End) |
          (nAryValueParser.!.rep(min = 1, sep = ",") ~ End))
    )
  ).map {
    case NAryAtomFromTuple(atom) => atom
  }

  private val atomParser: Parser[SearchFilterExpr.Atom] = P(binaryAtomParser | nAryAtomParser)

  @deprecated("play-akka transition", "0")
  def parse(query: Map[String, Seq[String]]): Try[SearchFilterExpr] =
    parse(query.toSeq.flatMap {
      case (key, values) =>
        values.map(value => key -> value)
    })

  def parse(query: Seq[(String, String)]): Try[SearchFilterExpr] = Try {
    query.toList.collect { case ("filters", value) => value } match {
      case Nil => SearchFilterExpr.Empty

      case head :: Nil =>
        atomParser.parse(head) match {
          case Parsed.Success(x, _)    => x
          case e: Parsed.Failure[_, _] => throw new ParseQueryArgException("filters" -> formatFailure(1, e))
        }

      case xs =>
        val parsed = xs.map(x => atomParser.parse(x))
        val failures: Seq[String] = parsed.zipWithIndex.collect {
          case (e: Parsed.Failure[_, _], index) => formatFailure(index, e)
        }

        if (failures.isEmpty) {
          val filters = parsed.collect {
            case Parsed.Success(x, _) => x
          }

          SearchFilterExpr.Intersection.create(filters: _*)
        } else {
          throw new ParseQueryArgException("filters" -> failures.mkString("; "))
        }
    }
  }

  private def formatFailure(sectionIndex: Int, e: Parsed.Failure[_, _]): String = {
    s"section $sectionIndex: ${fastparse.core.ParseError.msg(e.extra.input, e.extra.traced.expected, e.index)}"
  }

}
