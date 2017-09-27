package xyz.driver.pdsuicommon.parsers

import xyz.driver.pdsuicommon.utils.Implicits.{toCharOps, toStringOps}
import fastparse.all._
import fastparse.core.Parsed
import fastparse.parsers.Intrinsics.CharPred
import xyz.driver.pdsuicommon.db.{SearchFilterBinaryOperation, SearchFilterExpr, SearchFilterNAryOperation}
import xyz.driver.pdsuicommon.utils.Utils._

import scala.util.Try

@SuppressWarnings(Array("org.wartremover.warts.Product", "org.wartremover.warts.Serializable"))
object SearchFilterParser {

  private object BinaryAtomFromTuple {
    def unapply(input: (SearchFilterExpr.Dimension, (String, Any))): Option[SearchFilterExpr.Atom.Binary] = {
      val (dimensionName, (strOperation, value)) = input
      val updatedValue = value match {
        case s: String => s.safeTrim
        case a => a
      }

      parseOperation(strOperation.toLowerCase).map { op =>
        SearchFilterExpr.Atom.Binary(dimensionName, op, updatedValue.asInstanceOf[AnyRef])
      }
    }
  }

  private object NAryAtomFromTuple {
    // Compiler warning: unchecked since it is eliminated by erasure, if we user Seq[String]
    def unapply(input: (SearchFilterExpr.Dimension, (String, Seq[_]))): Option[SearchFilterExpr.Atom.NAry] = {
      val (dimensionName, (strOperation, xs)) = input
      if (strOperation.toLowerCase == "in") {
        val values = xs.asInstanceOf[Seq[String]].map(_.safeTrim)
        Some(SearchFilterExpr.Atom.NAry(dimensionName, SearchFilterNAryOperation.In, values))
      } else {
        None
      }
    }
  }

  private val operationsMapping = {
    import xyz.driver.pdsuicommon.db.SearchFilterBinaryOperation._

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

  private val whitespaceParser = P(CharPred(_.isSafeWhitespace))

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
    P(IgnoreCase("eq") | ((IgnoreCase("gt") | IgnoreCase("lt")) ~ IgnoreCase("eq").?)).!
  }

  private val naryOperatorParser: Parser[String] = P(IgnoreCase("in")).!

  private val isPositiveParser: Parser[Boolean] = P(CharIn("-+").!.?).map {
    case Some("-") => false
    case _         => true
  }

  // Exclude Unicode "digits"
  private val digitsParser: Parser[String] = P(CharIn('0' to '9').rep(min = 1).!)

  // @TODO Make complex checking here
  private val numberParser: Parser[String] = P(isPositiveParser ~ digitsParser.! ~ ("." ~ digitsParser).!.?).map {
    case (false, intPart, Some(fracPart)) => s"-$intPart.${fracPart.tail}"
    case (false, intPart, None)           => s"-$intPart"
    case (_, intPart, Some(fracPart))     => s"$intPart.${fracPart.tail}"
    case (_, intPart, None)               => s"$intPart"
  }

  private val nAryValueParser: Parser[String] = P(CharPred(_ != ',').rep(min = 1).!)

  private val longParser: Parser[Long] = P(CharIn('0'to'9').rep(1).!.map(_.toLong))

  private val binaryAtomParser: Parser[SearchFilterExpr.Atom.Binary] = P(
    dimensionParser ~ whitespaceParser ~ (
      (numericOperatorParser.! ~ whitespaceParser ~ (longParser | numberParser.!)) |
      (commonOperatorParser.! ~ whitespaceParser ~ AnyChar.rep(min = 1).!)
    ) ~ End
  ).map {
    case BinaryAtomFromTuple(atom) => atom
  }

  private val nAryAtomParser: Parser[SearchFilterExpr.Atom.NAry] = P(
    dimensionParser ~ whitespaceParser ~ (
      naryOperatorParser ~/ whitespaceParser ~/ nAryValueParser.!.rep(min = 1, sep = ",")
    ) ~ End
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
          case Parsed.Success(x, _) => x
          case e: Parsed.Failure    => throw new ParseQueryArgException("filters" -> formatFailure(1, e))
        }

      case xs =>
        val parsed = xs.map(x => atomParser.parse(x))
        val failures: Seq[String] = parsed.zipWithIndex.collect {
          case (e: Parsed.Failure, index) => formatFailure(index, e)
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

  private def formatFailure(sectionIndex: Int, e: Parsed.Failure): String = {
    s"section $sectionIndex: ${ParseError.msg(e.extra.input, e.extra.traced.expected, e.index)}"
  }

}
