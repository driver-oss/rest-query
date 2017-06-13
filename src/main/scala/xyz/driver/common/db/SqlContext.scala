package xyz.driver.common.db

import java.io.Closeable
import java.net.URI
import java.time._
import java.util.UUID
import java.util.concurrent.Executors
import javax.sql.DataSource

import xyz.driver.common.logging.{PhiLogging, Unsafe}
import xyz.driver.common.concurrent.MdcExecutionContext
import xyz.driver.common.db.SqlContext.Settings
import xyz.driver.common.domain._
import xyz.driver.common.error.IncorrectIdException
import xyz.driver.common.utils.JsonSerializer
import com.typesafe.config.Config
import io.getquill._

import scala.concurrent.ExecutionContext
import scala.util.control.NonFatal
import scala.util.{Failure, Success, Try}

object SqlContext extends PhiLogging {

  case class DbCredentials(user: String,
                           password: String,
                           host: String,
                           port: Int,
                           dbName: String,
                           dbCreateFlag: Boolean,
                           dbContext: String,
                           connectionParams: String,
                           url: String)

  case class Settings(credentials: DbCredentials,
                      connection: Config,
                      connectionAttemptsOnStartup: Int,
                      threadPoolSize: Int)

  def apply(settings: Settings): SqlContext = {
    // Prevent leaking credentials to a log
    Try(JdbcContextConfig(settings.connection).dataSource) match {
      case Success(dataSource) => new SqlContext(dataSource, settings)
      case Failure(NonFatal(e)) =>
        logger.error(phi"Can not load dataSource, error: ${Unsafe(e.getClass.getName)}")
        throw new IllegalArgumentException("Can not load dataSource from config. Check your database and config")
    }
  }

}

class SqlContext(dataSource: DataSource with Closeable, settings: Settings)
  extends MysqlJdbcContext[MysqlEscape](dataSource)
    with EntityExtractorDerivation[Literal] {

  private val tpe = Executors.newFixedThreadPool(settings.threadPoolSize)

  implicit val executionContext: ExecutionContext = {
    val orig = ExecutionContext.fromExecutor(tpe)
    MdcExecutionContext.from(orig)
  }

  override def close(): Unit = {
    super.close()
    tpe.shutdownNow()
  }

  // ///////// Encodes/Decoders ///////////

  /**
    * Overrode, because Quill JDBC optionDecoder pass null inside decoders.
    * If custom decoder don't have special null handler, it will failed.
    *
    * @see https://github.com/getquill/quill/issues/535
    */
  implicit override def optionDecoder[T](implicit d: Decoder[T]): Decoder[Option[T]] =
    decoder(
      sqlType = d.sqlType,
      row => index => {
        try {
          val res = d(index - 1, row)
          if (row.wasNull) {
            None
          }
          else {
            Some(res)
          }
        } catch {
          case _: NullPointerException => None
          case _: IncorrectIdException => None
        }
      }
    )

  implicit def encodeStringId[T] = MappedEncoding[StringId[T], String](_.id)
  implicit def decodeStringId[T] = MappedEncoding[String, StringId[T]] {
    case "" => throw IncorrectIdException("'' is an invalid Id value")
    case x => StringId(x)
  }

  def decodeOptStringId[T] = MappedEncoding[Option[String], Option[StringId[T]]] {
    case None | Some("") => None
    case Some(x) => Some(StringId(x))
  }

  implicit def encodeLongId[T] = MappedEncoding[LongId[T], Long](_.id)
  implicit def decodeLongId[T] = MappedEncoding[Long, LongId[T]] {
    case 0 => throw IncorrectIdException("0 is an invalid Id value")
    case x => LongId(x)
  }

  // TODO Dirty hack, see REP-475
  def decodeOptLongId[T] = MappedEncoding[Option[Long], Option[LongId[T]]] {
    case None | Some(0) => None
    case Some(x) => Some(LongId(x))
  }

  implicit def encodeUuidId[T] = MappedEncoding[UuidId[T], String](_.toString)
  implicit def decodeUuidId[T] = MappedEncoding[String, UuidId[T]] {
    case "" => throw IncorrectIdException("'' is an invalid Id value")
    case x => UuidId(x)
  }

  def decodeOptUuidId[T] = MappedEncoding[Option[String], Option[UuidId[T]]] {
    case None | Some("") => None
    case Some(x) => Some(UuidId(x))
  }

  implicit def encodeTextJson[T: Manifest] = MappedEncoding[TextJson[T], String](x => JsonSerializer.serialize(x.content))
  implicit def decodeTextJson[T: Manifest] = MappedEncoding[String, TextJson[T]] { x =>
    TextJson(JsonSerializer.deserialize[T](x))
  }

  implicit val encodeUserRole = MappedEncoding[User.Role, Int](_.bit)
  implicit val decodeUserRole = MappedEncoding[Int, User.Role] {
    // 0 is treated as null for numeric types
    case 0 => throw new NullPointerException("0 means no roles. A user must have a role")
    case x => User.Role(x)
  }

  implicit val encodeEmail = MappedEncoding[Email, String](_.value.toString)
  implicit val decodeEmail = MappedEncoding[String, Email](Email)

  implicit val encodePasswordHash = MappedEncoding[PasswordHash, Array[Byte]](_.value)
  implicit val decodePasswordHash = MappedEncoding[Array[Byte], PasswordHash](PasswordHash(_))

  implicit val encodeUri = MappedEncoding[URI, String](_.toString)
  implicit val decodeUri = MappedEncoding[String, URI](URI.create)

  implicit val encodeCaseId = MappedEncoding[CaseId, String](_.id.toString)
  implicit val decodeCaseId = MappedEncoding[String, CaseId](CaseId(_))

  implicit val encodeFuzzyValue = {
    MappedEncoding[FuzzyValue, String] {
      case FuzzyValue.No => "No"
      case FuzzyValue.Yes => "Yes"
      case FuzzyValue.Maybe => "Maybe"
    }
  }
  implicit val decodeFuzzyValue = MappedEncoding[String, FuzzyValue] {
    case "Yes" => FuzzyValue.Yes
    case "No" => FuzzyValue.No
    case "Maybe" => FuzzyValue.Maybe
    case x =>
      Option(x).fold {
        throw new NullPointerException("FuzzyValue is null") // See catch in optionDecoder
      } { _ =>
        throw new IllegalStateException(s"Unknown fuzzy value: $x")
      }
  }


  implicit val encodeRecordRequestId = MappedEncoding[RecordRequestId, String](_.id.toString)
  implicit val decodeRecordRequestId = MappedEncoding[String, RecordRequestId] { x =>
    RecordRequestId(UUID.fromString(x))
  }

  final implicit class LocalDateTimeDbOps(val left: LocalDateTime) {

    // scalastyle:off
    def <=(right: LocalDateTime): Quoted[Boolean] = quote(infix"$left <= $right".as[Boolean])
  }

}
