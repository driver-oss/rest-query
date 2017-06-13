package xyz.driver.pdsuicommon

import java.time.{LocalDateTime, ZoneId}

import org.scalatest.FreeSpecLike
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{Millis, Span}
import xyz.driver.pdsuicommon.db.{MysqlQueryBuilder, SearchFilterExpr, SqlContext, Transactions}
import xyz.driver.pdsuicommon.domain.{Email, LongId, PasswordHash, User}
import xyz.driver.pdsuicommon.error.UnexpectedFilterException
import xyz.driver.pdsuicommon.utils.DiffUtils

import scala.concurrent.ExecutionContext.Implicits._
import scala.concurrent.Future

trait BaseSuite extends FreeSpecLike with DiffUtils with ScalaFutures {

  implicit val defaultPatience = PatienceConfig(timeout = Span(1000, Millis), interval = Span(20, Millis))
  implicit val sqlContext      = new MockSqlContext(global)

  def sampleUser(role: User.Role, email: String = "test@example.com", password: String = "123") = User(
    id = LongId(2001),
    email = Email(email),
    name = "Test",
    role = role,
    passwordHash = PasswordHash(password),
    latestActivity = Some(LocalDateTime.now(ZoneId.of("Z"))),
    deleted = None
  )

  def createMockQueryBuilder[T](isExpectedFilter: SearchFilterExpr => Boolean): MysqlQueryBuilder[T] = {
    MockQueryBuilder[T] {
      case (filter, _, _) if isExpectedFilter(filter) =>
        Future.successful(Seq.empty)
      case (filter, _, _) =>
        Future.failed(new UnexpectedFilterException(s"Filter is unexpected: $filter"))
    } {
      case _ =>
        Future.successful((0, Option.empty[LocalDateTime]))
    }
  }

  def transactions = new Transactions {
    override def run[T](f: (SqlContext) => T): Future[T] = {
      Future(f(sqlContext))
    }
  }

}
