package xyz.driver.pdsuicommon.logging

import java.time.{LocalDateTime, ZoneId}

import xyz.driver.core.auth.User

object TimeLogger extends PhiLogging {

  def logTime(userId: xyz.driver.core.Id[User], label: String, obj: String): Unit = {
    val now = LocalDateTime.now(ZoneId.of("Z"))
    logger.info(phi"User id=${Unsafe(userId)} performed an action at ${Unsafe(label)}=$now with a ${Unsafe(obj)} ")
  }
}
