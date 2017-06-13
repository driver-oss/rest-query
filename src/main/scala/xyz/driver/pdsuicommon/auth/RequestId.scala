package xyz.driver.pdsuicommon.auth

import xyz.driver.pdsuicommon.logging._
import xyz.driver.pdsuicommon.auth.RequestId._
import xyz.driver.pdsuicommon.utils.RandomUtils

final case class RequestId(value: String = RandomUtils.randomString(IdLength))

object RequestId {

  private val IdLength = 20

  implicit def toPhiString(x: RequestId): PhiString = phi"RequestId(${Unsafe(x.value)})"
}
