package xyz.driver.pdsuicommon.error

import java.io.{ByteArrayOutputStream, PrintStream}

object ExceptionFormatter {

  def format(e: Throwable): String = s"$e\n${printStackTrace(e)}"

  def printStackTrace(e: Throwable): String = {
    val baos = new ByteArrayOutputStream()
    val ps   = new PrintStream(baos)

    e.printStackTrace(ps)

    ps.close()
    baos.toString()
  }
}
