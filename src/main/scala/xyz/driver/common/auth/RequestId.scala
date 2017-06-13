package xyz.driver.common.auth

import xyz.driver.common.logging._
import xyz.driver.common.auth.RequestId._
import xyz.driver.common.utils.RandomUtils

final case class RequestId(value: String = RandomUtils.randomString(IdLength))

object RequestId {

  private val IdLength = 20

  implicit def toPhiString(x: RequestId): PhiString = phi"RequestId(${Unsafe(x.value)})"

}
