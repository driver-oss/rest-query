package xyz.driver.common

import java.net.URL

import scala.concurrent.Future

package object http {
  type HttpFetcher = URL => Future[Array[Byte]]
}
