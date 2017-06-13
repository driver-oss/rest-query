package xyz.driver.common.http

import java.io.Closeable
import java.net.URI
import java.util.concurrent.{ExecutorService, Executors}

import xyz.driver.common.concurrent.MdcThreadFactory
import xyz.driver.common.http.AsyncHttpClientUploader._
import xyz.driver.common.utils.RandomUtils
import com.typesafe.scalalogging.StrictLogging
import org.asynchttpclient._
import org.slf4j.MDC

import scala.concurrent.duration.FiniteDuration
import scala.concurrent.{ExecutionContext, Future, Promise}

class AsyncHttpClientUploader(settings: Settings) extends Closeable with StrictLogging {

  private val es: ExecutorService = {
    val threadFactory = MdcThreadFactory.from(Executors.defaultThreadFactory())
    Executors.newSingleThreadExecutor(threadFactory)
  }

  private implicit val executionContext = ExecutionContext.fromExecutor(es)

  private def httpClientConfig: DefaultAsyncHttpClientConfig = {
    val builder = new DefaultAsyncHttpClientConfig.Builder()
    builder.setConnectTimeout(settings.connectTimeout.toMillis.toInt)
    builder.setRequestTimeout(settings.requestTimeout.toMillis.toInt)
    // builder.setThreadFactory(threadFactory) // Doesn't help to push MDC context into AsyncCompletionHandler
    builder.build()
  }

  private val httpClient = new DefaultAsyncHttpClient(httpClientConfig)

  def run(method: Method, uri: URI, contentType: String, data: String): Future[Unit] = {
    // log all outcome connections
    val fingerPrint = RandomUtils.randomString(10)
    logger.info("{}, apply(method={}, uri={}, contentType={})", fingerPrint, method, uri, contentType)
    val promise = Promise[Response]()

    val q = new RequestBuilder(method.toString)
      .setUrl(uri.toString)
      .setBody(data)

    settings.defaultHeaders.foreach {
      case (k, v) =>
        q.setHeader(k, v)
    }

    q.addHeader("Content-Type", contentType)

    httpClient.prepareRequest(q).execute(new AsyncCompletionHandler[Unit] {
      override def onCompleted(response: Response): Unit = {
        promise.success(response)
      }

      override def onThrowable(t: Throwable): Unit = {
        promise.failure(t)
        super.onThrowable(t)
      }
    })

    // see AsyncHttpClientFetcher
    val parentMdcContext = MDC.getCopyOfContextMap
    promise.future.flatMap { response =>
      setContextMap(parentMdcContext)

      val statusCode = response.getStatusCode
      // https://en.wikipedia.org/wiki/List_of_HTTP_status_codes#2xx_Success
      if (statusCode >= 200 && statusCode < 300) {
        logger.debug("{}, success", fingerPrint)
        Future.successful(())
      } else {
        logger.error(
          "{}, HTTP {}, BODY:\n{}",
          fingerPrint,
          response.getStatusCode.asInstanceOf[AnyRef],
          response.getResponseBody.take(100)
        )
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

object AsyncHttpClientUploader {

  case class Settings(connectTimeout: FiniteDuration,
                      requestTimeout: FiniteDuration,
                      defaultHeaders: Map[String, String] = Map.empty)

  sealed trait Method

  object Method {

    case object Put extends Method {
      override val toString = "PUT"
    }

    case object Post extends Method {
      override val toString = "POST"
    }

  }

}
