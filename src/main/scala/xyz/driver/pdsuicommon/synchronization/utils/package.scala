package xyz.driver.pdsuicommon.synchronization

import java.net.URL
import java.nio.ByteBuffer
import java.util.UUID

import xyz.driver.pdsuicommon.domain.{LongId, UuidId}
import xyz.driver.pdsuicommon.http.HttpFetcher
import xyz.driver.pdsuicommon.json.JsonSerializer
import xyz.driver.pdsuicommon.synchronization.domain.FakeId

import scala.collection.breakOut
import scala.concurrent.{ExecutionContext, Future}
import scala.io.Codec

package object utils {

  type FakeIdMap[T] = Map[FakeId, T]

  object FakeIdMap {

    def empty[T]: FakeIdMap[T] = Map.empty

    def create[T](xs: Seq[T])(implicit fakeIdExtractor: FakeIdGen[T]): FakeIdMap[T] = {
      xs.map({ x =>
        fakeIdExtractor.getFor(x) -> x
      })(breakOut)
    }

  }

  /**
    * Requests domain objects from the repository using
    * ids of fetched dictionary entities
    *
    * @param getList repository access function
    * @param xs sequence of entity objects
    * @param id function that extracts id from the entity
    * @tparam Id Type of Id (for example [[LongId]], [[UuidId]])
    * @tparam K Type parameter for Id
    * @tparam D Domain object type name
    * @tparam E Dictionary entity object type name
    */
  def domainFromEntities[K, D, E, Id[_]](getList: Set[Id[K]] => Seq[D], xs: Seq[E])(id: E => Id[K]): Seq[D] = {
    getList(xs.map(x => id(x)).toSet)
  }

  /** Version of [[domainFromEntities]] for LongId */
  def domainFromEntitiesLong[K, D, E](getList: Set[LongId[K]] => Seq[D], xs: Seq[E])(id: E => Long): Seq[D] = {
    domainFromEntities(getList, xs)(e => LongId(id(e)))
  }

  /** Version of [[domainFromEntities]] for UuidId */
  def domainFromEntitiesUUID[K, D, E](getList: Set[UuidId[K]] => Seq[D], xs: Seq[E])(id: E => UUID): Seq[D] = {
    domainFromEntities(getList, xs)(e => UuidId(id(e)))
  }

  def fetch[T](httpFetcher: HttpFetcher, url: URL)(implicit m: Manifest[T], ec: ExecutionContext): Future[T] = {
    httpFetcher(url).map { rawContent =>
      val content = Codec.UTF8.decoder.decode(ByteBuffer.wrap(rawContent)).toString
      JsonSerializer.deserialize[T](content)
    }
  }
}
