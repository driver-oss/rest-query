package xyz.driver.pdsuicommon.parsers

import org.scalacheck.Prop
import org.scalacheck.Prop.BooleanOperators
import org.scalatest.matchers.{MatchResult, Matcher}
import xyz.driver.pdsuicommon.utils.Utils

import scala.reflect.ClassTag
import scala.util.{Failure, Success, Try}

object TestUtils {

  object success extends Matcher[Try[Any]] {
    override def apply(left: Try[Any]) = {
      MatchResult(left.isSuccess, s"$left did not fail", s"did fail with $left")
    }
  }

  class FailWith[ThrowableT <: Throwable](implicit ct: ClassTag[ThrowableT]) extends Matcher[Try[Any]] {
    override def apply(left: Try[Any]): MatchResult = {
      MatchResult(
        left.isFailure && left.failed.get.getClass == ct.runtimeClass,
        left match {
          case Success(_) => s"$left did not fail"
          case Failure(e) =>
            s"$left did fail with ${Utils.getClassSimpleName(e.getClass)}, " +
              s"not ${Utils.getClassSimpleName(ct.runtimeClass)}"
        },
        left match {
          case Success(_) => s"$left failed with ${Utils.getClassSimpleName(ct.runtimeClass)}"
          case Failure(e) => s"$left failed with ${Utils.getClassSimpleName(e.getClass)}"
        }
      )
    }
  }

  def failWith[ThrowableT <: Throwable](implicit ct: ClassTag[ThrowableT]) = new FailWith[ThrowableT]

  final implicit class TryPropOps(val self: Try[Any]) extends AnyVal {

    def successProp: Prop = self match {
      case Success(_) => true :| "ok"
      case Failure(e) => false :| e.getMessage
    }

    def failureProp: Prop = self match {
      case Success(x) => false :| s"invalid: $x"
      case Failure(_) => true
    }

  }

}
