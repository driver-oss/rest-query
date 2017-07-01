package xyz.driver.pdsuidomain.formats.json

/*
import java.time.LocalDateTime

import org.scalatest.FreeSpecLike
import play.api.libs.json.Json

import scala.collection.breakOut

class ApiDocumentSuite extends FreeSpecLike {

  "ApiDocument" - {
    "conforms to specification when serialized" in {
      val document = ApiDocument(
        id = 1L,
        recordId = 123L,
        physician = Some("physician"),
        lastUpdate = LocalDateTime.now(),
        `type` = "some-type",
        startDate = LocalDateTime.now(),
        endDate = Some(LocalDateTime.now()),
        provider = "test-provider",
        providerType = "test-provider-type",
        status = "New",
        assignee = Some(5L),
        previousAssignee = None,
        meta = "{}"
      )

      val actualJson = serialize(document)
      val matcher =
        """^\{
          |"id":"[^"]+",
          |"recordId":"[^"]+",
          |"physician":"[^"]+",
          |"lastUpdate":"[^"]+",
          |"type":"[^"]+",
          |"startDate":"[^"]+",
          |"endDate":"[^"]+",
          |"provider":"[^"]+",
          |"providerType":"[^"]+",
          |"status":"[^"]+",
          |"assignee":"[^"]+",
          |"meta":\{[^\}]*\}
          |\}""".stripMargin.lines.mkString.r

      assert(
        matcher.pattern.matcher(actualJson).matches(),
        s"""see https://driverinc.atlassian.net/wiki/display/RA/REST+API+Specification#RESTAPISpecification-DocumentObject
            |pattern = ${matcher.pattern}
            |json = $actualJson""".stripMargin
      )
    }

    "has optional fields according to specification" in {
      val expectedOptionalFields = Set("physician", "endDate", "assignee", "previousAssignee")

      val klass = classOf[ApiDocument]
      val actualOptionalFields: Set[String] = klass.getDeclaredFields.collect({
        case x if x.getType == classOf[Option[_]] => x.getName
      })(breakOut)

      assert(
        actualOptionalFields === expectedOptionalFields,
        """actual vs expected
          |see https://driverinc.atlassian.net/wiki/display/RA/REST+API+Specification#RESTAPISpecification-DocumentObject
        """.stripMargin.trim
      )
    }
  }

  private def serialize(document: ApiDocument): String = {
    Json.stringify(Json.toJson(document))
  }

}
 */
