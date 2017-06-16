package xyz.driver.pdsuicommon.http

import java.io.Closeable
import java.net.URL
import java.util.concurrent.{ExecutorService, Executors}

import com.typesafe.scalalogging.StrictLogging
import org.asynchttpclient._
import org.slf4j.MDC
import xyz.driver.pdsuicommon.concurrent.MdcThreadFactory
import xyz.driver.pdsuicommon.utils.RandomUtils

import scala.concurrent.duration.FiniteDuration
import scala.concurrent.{ExecutionContext, Future, Promise}

class AsyncHttpClientFetcher(settings: AsyncHttpClientFetcher.Settings)
    extends HttpFetcher with Closeable with StrictLogging {

  private val es: ExecutorService = {
    val threadFactory = MdcThreadFactory.from(Executors.defaultThreadFactory())
    Executors.newSingleThreadExecutor(threadFactory)
  }

  private implicit val executionContext = ExecutionContext.fromExecutor(es)

  private def httpClientConfig: DefaultAsyncHttpClientConfig = {
    val builder = new DefaultAsyncHttpClientConfig.Builder()
    builder.setConnectTimeout(settings.connectTimeout.toMillis.toInt)
    builder.setReadTimeout(settings.readTimeout.toMillis.toInt)
    // builder.setThreadFactory(threadFactory) // Doesn't help to push MDC context into AsyncCompletionHandler
    builder.build()
  }

  private val httpClient = new DefaultAsyncHttpClient(httpClientConfig)

  override def apply(url: URL): Future[Array[Byte]] = {
    val fingerPrint = RandomUtils.randomString(10)

    // log all outcome connections
    logger.info("{}, apply({})", fingerPrint, url)
    val promise = Promise[Response]()

    httpClient
      .prepareGet(url.toString)
      .execute(new AsyncCompletionHandler[Response] {
        override def onCompleted(response: Response): Response = {
          promise.success(response)
          response
        }

        override def onThrowable(t: Throwable): Unit = {
          promise.failure(t)
          super.onThrowable(t)
        }
      })

    // Promises have their own ExecutionContext
    // So, we have to hack it.
    val parentMdcContext = MDC.getCopyOfContextMap
    promise.future.flatMap { response =>
      setContextMap(parentMdcContext)

      if (response.getStatusCode == 200) {
        // DO NOT LOG body, it could be PHI
        val bytes = response.getResponseBodyAsBytes
        logger.debug("{}, size is {}B", fingerPrint, bytes.size.asInstanceOf[AnyRef])
        Future.successful(bytes)
      } else {
        logger.error("{}, HTTP {}", fingerPrint, response.getStatusCode.asInstanceOf[AnyRef])
        logger.trace(response.getResponseBody().take(100))
        Future.failed(new IllegalStateException("An unexpected response from the server"))
      }
    }
  }

  private[this] def setContextMap(context: java.util.Map[String, String]): Unit =
    Option(context).fold(MDC.clear())(MDC.setContextMap)

  override def close(): Unit = {
    httpClient.close()
    es.shutdown()
  }

}

object AsyncHttpClientFetcher {

  case class Settings(connectTimeout: FiniteDuration, readTimeout: FiniteDuration)

}
