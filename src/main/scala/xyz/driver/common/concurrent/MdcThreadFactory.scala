package xyz.driver.common.concurrent

import java.util.concurrent.ThreadFactory

import org.slf4j.MDC

object MdcThreadFactory {
  def from(orig: ThreadFactory): ThreadFactory = new MdcThreadFactory(orig)
}

class MdcThreadFactory(orig: ThreadFactory) extends ThreadFactory {

  override def newThread(runnable: Runnable): Thread = {
    val parentMdcContext = MDC.getCopyOfContextMap

    orig.newThread(new Runnable {
      def run(): Unit = {
        val saveMdcContext = MDC.getCopyOfContextMap
        setContextMap(parentMdcContext)

        try {
          runnable.run()
        } finally {
          setContextMap(saveMdcContext)
        }
      }
    })
  }

  private[this] def setContextMap(context: java.util.Map[String, String]): Unit =
    Option(context).fold(MDC.clear())(MDC.setContextMap)

}
