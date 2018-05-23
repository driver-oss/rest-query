package xyz.driver.restquery.rest.parsers

import java.time.LocalDate
import java.util.UUID

import fastparse.all._
import fastparse.core.Parsed
import xyz.driver.restquery.query.{SearchFilterBinaryOperation, SearchFilterExpr, SearchFilterNAryOperation}
import xyz.driver.restquery.utils.Utils
import xyz.driver.restquery.utils.Utils._

import scala.util.Try

@SuppressWarnings(Array("org.wartremover.warts.Product", "org.wartremover.warts.Serializable"))
object SearchFilterParser {

  private object BinaryAtomFromTuple {
    def unapply(input: (SearchFilterExpr.Dimension, (String, Any))): Option[SearchFilterExpr.Atom.Binary] = {
      val (dimensionName, (strOperation, value)) = input
      val updatedValue                           = trimIfString(value)

      parseBinaryOperation(strOperation.toLowerCase).map { op =>
        SearchFilterExpr.Atom.Binary(dimensionName, op, updatedValue.asInstanceOf[AnyRef])
      }
    }
  }

  private object NAryAtomFromTuple {
    // Compiler warning: unchecked since it is eliminated by erasure, if we user Seq[String]
    def unapply(input: (SearchFilterExpr.Dimension, (String, Seq[_]))): Option[SearchFilterExpr.Atom.NAry] = {
      val (dimensionName, (strOperation, xs)) = input
      val updatedValues                       = xs.map(trimIfString)

      parseNAryOperation(strOperation.toLowerCase).map(
        op =>
          SearchFilterExpr.Atom
            .NAry(dimensionName, op, updatedValues.map(_.asInstanceOf[AnyRef])))
    }
  }

  private def trimIfString(value: Any) =
    value match {
      case s: String => Utils.safeTrim(s)
      case a         => a
    }

  private def parseBinaryOperation: String => Option[SearchFilterBinaryOperation] =
    SearchFilterBinaryOperation.binaryOperationsFromString.get

  private def parseNAryOperation: String => Option[SearchFilterNAryOperation] =
    SearchFilterNAryOperation.nAryOperationsFromString.get

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
    import xyz.driver.restquery.query.SearchFilterBinaryOperation.binaryOperationToName
    val eq    = binaryOperationToName(SearchFilterBinaryOperation.Eq)
    val like  = binaryOperationToName(SearchFilterBinaryOperation.Like)
    val noteq = binaryOperationToName(SearchFilterBinaryOperation.NotEq)
    P(IgnoreCase(eq) | IgnoreCase(like) | IgnoreCase(noteq)).!
  }

  private val numericOperatorParser: Parser[String] = {
    import xyz.driver.restquery.query.SearchFilterBinaryOperation.binaryOperationToName
    val eq    = binaryOperationToName(SearchFilterBinaryOperation.Eq)
    val noteq = binaryOperationToName(SearchFilterBinaryOperation.NotEq)
    val gt    = binaryOperationToName(SearchFilterBinaryOperation.Gt)
    val lt    = binaryOperationToName(SearchFilterBinaryOperation.Lt)
    P(IgnoreCase(eq) | IgnoreCase(noteq) | ((IgnoreCase(gt) | IgnoreCase(lt)) ~ IgnoreCase(eq).?)).!
  }

  private val naryOperatorParser: Parser[String] = {
    import xyz.driver.restquery.query.SearchFilterNAryOperation.nAryOperationToName
    val in    = nAryOperationToName(SearchFilterNAryOperation.In)
    val notin = nAryOperationToName(SearchFilterNAryOperation.NotIn)
    P(IgnoreCase(in) | IgnoreCase(notin)).!
  }

  private val isPositiveParser: Parser[Boolean] = P(CharIn("-+").!.?).map {
    case Some("-") => false
    case _         => true
  }

  private val digitsParser: Parser[String] = P(CharIn('0' to '9').rep(min = 1).!) // Exclude Unicode "digits"

  private val numberParser: Parser[String] =
    P(isPositiveParser ~ digitsParser.! ~ ("." ~ digitsParser).!.?).map {
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
      hexDigit.rep(8).! ~ "-" ~ hexDigit.rep(4).! ~ "-" ~ hexDigit
        .rep(4)
        .! ~ "-" ~ hexDigit.rep(4).! ~ "-" ~ hexDigit
        .rep(12)
        .!).map {
      case (group1, group2, group3, group4, group5) =>
        UUID.fromString(s"$group1-$group2-$group3-$group4-$group5")
    }

  private val dateParser: Parser[LocalDate] =
    P(CharIn('0' to '9').rep(min = 4).! ~ "-" ~ CharIn('0' to '9').rep(1).! ~ "-" ~ CharIn('0' to '9').rep(1).!)
      .map {
        case (year, month, day) =>
          LocalDate.of(year.toInt, month.toInt, day.toInt)
      }

  private val binaryAtomParser: Parser[SearchFilterExpr.Atom.Binary] = P(
    dimensionParser ~ whitespaceParser ~
      ((numericOperatorParser.! ~ whitespaceParser ~ (dateParser | longParser | numberParser.!) ~ End) |
        (commonOperatorParser.! ~ whitespaceParser ~ (uuidParser | booleanParser | AnyChar
          .rep(min = 1)
          .!) ~ End))
  ).map {
    case BinaryAtomFromTuple(atom) => atom
  }

  private val nAryAtomParser: Parser[SearchFilterExpr.Atom.NAry] = P(
    dimensionParser ~ whitespaceParser ~ (
      naryOperatorParser ~ whitespaceParser ~
        ((dateParser.rep(min = 1, sep = ",") ~ End) |
          (uuidParser.rep(min = 1, sep = ",") ~ End) |
          (longParser.rep(min = 1, sep = ",") ~ End) |
          (booleanParser.rep(min = 1, sep = ",") ~ End) |
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
          case Parsed.Success(x, _) => x
          case e: Parsed.Failure[_, _] =>
            throw new ParseQueryArgException("filters" -> formatFailure(1, e))
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
    s"section $sectionIndex: ${fastparse.core.ParseError
      .msg(e.extra.input, e.extra.traced.expected, e.index)}"
  }

}
