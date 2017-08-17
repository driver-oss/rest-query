package xyz.driver.pdsuicommon.db

import java.io.Closeable
import java.sql.Types
import java.time._
import java.util.UUID
import java.util.concurrent.Executors
import javax.sql.DataSource

import io.getquill._
import xyz.driver.pdsuicommon.concurrent.MdcExecutionContext
import xyz.driver.pdsuicommon.db.PostgresContext.Settings
import xyz.driver.pdsuicommon.domain.UuidId
import xyz.driver.pdsuicommon.logging._

import scala.concurrent.ExecutionContext
import scala.util.control.NonFatal
import scala.util.{Failure, Success, Try}

object PostgresContext extends PhiLogging {

  final case class Settings(connection: com.typesafe.config.Config,
                            connectionAttemptsOnStartup: Int,
                            threadPoolSize: Int)

  def apply(settings: Settings): PostgresContext = {
    // Prevent leaking credentials to a log
    Try(JdbcContextConfig(settings.connection).dataSource) match {
      case Success(dataSource) => new PostgresContext(dataSource, settings)
      case Failure(NonFatal(e)) =>
        logger.error(phi"Can not load dataSource, error: ${Unsafe(e.getClass.getName)}")
        throw new IllegalArgumentException("Can not load dataSource from config. Check your database and config")
    }
  }

}

class PostgresContext(val dataSource: DataSource with Closeable, settings: Settings)
    extends PostgresJdbcContext[SnakeCase](dataSource) with TransactionalContext
    with EntityExtractorDerivation[SnakeCase] {

  private val tpe = Executors.newFixedThreadPool(settings.threadPoolSize)

  implicit val executionContext: ExecutionContext = {
    val orig = ExecutionContext.fromExecutor(tpe)
    MdcExecutionContext.from(orig)
  }

  override def close(): Unit = {
    super.close()
    tpe.shutdownNow()
  }

  /**
    * Usable for QueryBuilder's extractors
    */
  def timestampToLocalDateTime(timestamp: java.sql.Timestamp): LocalDateTime = {
    LocalDateTime.ofInstant(timestamp.toInstant, ZoneOffset.UTC)
  }

  // Override localDateTime encoder and decoder cause
  // clinicaltrials.gov uses bigint to store timestamps

  override implicit val localDateTimeEncoder: Encoder[LocalDateTime] =
    encoder(Types.BIGINT,
            (index, value, row) => row.setLong(index, value.atZone(ZoneOffset.UTC).toInstant.toEpochMilli))

  override implicit val localDateTimeDecoder: Decoder[LocalDateTime] =
    decoder(
      Types.BIGINT,
      (index, row) => {
        row.getLong(index) match {
          case 0 => throw new NullPointerException("0 is decoded as null")
          case x => LocalDateTime.ofInstant(Instant.ofEpochMilli(x), ZoneId.of("Z"))
        }
      }
    )

  implicit def encodeUuidId[T] = MappedEncoding[UuidId[T], String](_.toString)
  implicit def decodeUuidId[T] = MappedEncoding[String, UuidId[T]] { uuid =>
    UuidId[T](UUID.fromString(uuid))
  }

  def decodeOptUuidId[T] = MappedEncoding[Option[String], Option[UuidId[T]]] {
    case Some(x) => Option(x).map(y => UuidId[T](UUID.fromString(y)))
    case None    => None
  }

  implicit def decodeUuid[T] = MappedEncoding[String, UUID] { uuid =>
    UUID.fromString(uuid)
  }
}
