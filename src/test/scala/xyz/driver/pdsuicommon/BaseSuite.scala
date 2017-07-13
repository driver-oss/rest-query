package xyz.driver.pdsuicommon

import java.time.{LocalDateTime, ZoneId}

import org.scalatest.FreeSpecLike
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{Millis, Span}
import xyz.driver.pdsuicommon.db._
import xyz.driver.pdsuicommon.domain._
import xyz.driver.pdsuicommon.error.UnexpectedFilterException
import xyz.driver.pdsuicommon.utils.DiffUtils

trait BaseSuite extends FreeSpecLike with DiffUtils with ScalaFutures {

  implicit val defaultPatience = PatienceConfig(timeout = Span(1000, Millis), interval = Span(20, Millis))
  implicit val sqlContext      = new MockMySqlContext()

  def sampleUser(role: User.Role, email: String = "test@example.com", password: String = "123") = User(
    id = StringId("2001"),
    email = Email(email),
    name = "Test",
    roles = Set(role),
    latestActivity = Some(LocalDateTime.now(ZoneId.of("Z"))),
    deleted = None
  )

  def createMockQueryBuilder[T](isExpectedFilter: SearchFilterExpr => Boolean): MysqlQueryBuilder[T] = {
    MockQueryBuilder[T] {
      case (filter, _, _) if isExpectedFilter(filter) => Seq.empty
      case (filter, _, _)                             => throw new UnexpectedFilterException(s"Filter is unexpected: $filter")
    } {
      case _ => (0, Option.empty[LocalDateTime])
    }
  }
}
