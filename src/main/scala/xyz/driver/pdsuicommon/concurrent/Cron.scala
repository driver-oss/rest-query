package xyz.driver.pdsuicommon.concurrent

import java.io.Closeable
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean
import java.util.{Timer, TimerTask}

import com.typesafe.scalalogging.StrictLogging
import org.slf4j.MDC
import xyz.driver.pdsuicommon.error.ExceptionFormatter
import xyz.driver.pdsuicommon.utils.RandomUtils

import scala.concurrent.duration.FiniteDuration
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

class Cron(settings: Cron.Settings) extends Closeable with StrictLogging {

  import Cron._

  private val timer = new Timer("cronTimer", true)

  private val jobs = ConcurrentHashMap.newKeySet[String]()

  def register(name: String)(job: () => Future[Unit])(implicit ec: ExecutionContext): Unit = {
    logger.trace("register({})", name)
    val disableList = settings.disable.split(",").map(_.trim).toList
    if (disableList.contains(name)) logger.info("The task '{}' is disabled", name)
    else {
      settings.intervals.get(name) match {
        case None =>
          logger.error("Can not find an interval for task '{}', check the settings", name)
          throw new IllegalArgumentException(s"Can not find an interval for task '$name', check the settings")

        case Some(period) =>
          logger.info("register a new task '{}' with a period of {}ms", name, period.toMillis.asInstanceOf[AnyRef])
          timer.schedule(new SingletonTask(name, job), 0, period.toMillis)
      }
    }

    jobs.add(name)
  }

  /**
    * Checks unused jobs
    */
  def verify(): Unit = {
    import scala.collection.JavaConversions.asScalaSet

    val unusedJobs = settings.intervals.keySet -- jobs.toSet
    unusedJobs.foreach { job =>
      logger.warn(s"The job '$job' is listed, but not registered or ignored")
    }
  }

  override def close(): Unit = {
    timer.cancel()
  }
}

object Cron {

  case class Settings(disable: String, intervals: Map[String, FiniteDuration])

  private class SingletonTask(taskName: String,
                              job: () => Future[Unit])
                             (implicit ec: ExecutionContext)
    extends TimerTask with StrictLogging {

    private val isWorking = new AtomicBoolean(false)

    override def run(): Unit = {
      if (isWorking.compareAndSet(false, true)) {
        MDC.put("userId", "cron")
        MDC.put("requestId", RandomUtils.randomString(15))

        logger.info("Start '{}'", taskName)
        Try {
          job()
            .andThen {
              case Success(_) => logger.info("'{}' is completed", taskName)
              case Failure(e) => logger.error(s"Job '{}' is failed: ${ExceptionFormatter.format(e)}", taskName)
            }
            .onComplete(_ => isWorking.set(false))
        } match {
          case Success(_) =>
          case Failure(e) =>
            logger.error("Can't start '{}'", taskName, e)
        }
      } else {
        logger.debug("The previous job '{}' is in progress", taskName)
      }
    }
  }
}
