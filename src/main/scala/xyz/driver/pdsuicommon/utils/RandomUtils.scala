package xyz.driver.pdsuicommon.utils

import java.util.concurrent.ThreadLocalRandom

import scala.collection._

object RandomUtils {

  private def Random = ThreadLocalRandom.current()

  private val chars: Seq[Char] = ('0' to '9') ++ ('a' to 'z')

  def randomString(len: Int): String = {
    (0 until len).map({ _ =>
      val i = Random.nextInt(0, chars.size)
      chars(i)
    })(breakOut)
  }
}
