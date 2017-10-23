package xyz.driver.pdsuicommon

import java.time.{LocalDateTime, ZoneId}

import org.scalatest.FreeSpecLike
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{Millis, Span}
import xyz.driver.pdsuicommon.domain._
import xyz.driver.pdsuicommon.utils.DiffUtils

trait BaseSuite extends FreeSpecLike with DiffUtils with ScalaFutures {

  implicit val defaultPatience = PatienceConfig(timeout = Span(1000, Millis), interval = Span(20, Millis))

  def sampleUser(role: User.Role, email: String = "test@example.com", password: String = "123") = User(
    id = StringId("2001"),
    email = Email(email),
    name = "Test",
    roles = Set(role),
    latestActivity = Some(LocalDateTime.now(ZoneId.of("Z"))),
    deleted = None
  )
}
