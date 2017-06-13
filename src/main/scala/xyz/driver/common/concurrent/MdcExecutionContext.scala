package xyz.driver.common.concurrent

import org.slf4j.MDC

import scala.concurrent.{ExecutionContext, ExecutionContextExecutor}

object MdcExecutionContext {
  def from(orig: ExecutionContext): ExecutionContext = new MdcExecutionContext(orig)
}

class MdcExecutionContext(orig: ExecutionContext) extends ExecutionContextExecutor {

  def execute(runnable: Runnable): Unit = {
    val parentMdcContext = MDC.getCopyOfContextMap

    orig.execute(new Runnable {
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

  def reportFailure(t: Throwable): Unit = orig.reportFailure(t)

}
