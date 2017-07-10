package xyz.driver.pdsuicommon.logging

import java.time.{LocalDateTime, ZoneId}

import xyz.driver.pdsuicommon.domain.{LongId, User}

object TimeLogger extends PhiLogging {

  def logTime(userId: LongId[User], label: String, obj: String): Unit = {
    val now = LocalDateTime.now(ZoneId.of("Z"))
    logger.info(phi"User id=$userId performed an action at ${Unsafe(label)}=$now with a ${Unsafe(obj)} ")
  }
}