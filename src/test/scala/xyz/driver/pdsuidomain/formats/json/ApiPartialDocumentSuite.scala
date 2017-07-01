package xyz.driver.pdsuidomain.formats.json

/*
import java.time.LocalDateTime

import xyz.driver.pdsuicommon.domain._
import org.davidbild.tristate.Tristate
import org.scalatest.FreeSpecLike
import play.api.libs.json.Json

class ApiPartialDocumentSuite extends FreeSpecLike {

  "serialization" - {
    "can be deserialized" in deserialize(defaultJson)

    "endDate" - {
      def jsonWithEndDate(rawValue: String) = json("endDate" -> rawValue)
      def jsonWithoutEndDate = json()

      "when specified" - {
        "if non-empty object - should be parsed as date" in {
          val rawEndDate = "2007-12-03T10:15:30.000"
          val apiPartialEntity = deserialize(jsonWithEndDate(quoted(rawEndDate)))
          assert(apiPartialEntity.endDate.exists(_ == LocalDateTime.parse(rawEndDate)))
        }

        "if null - should be parsed as Absent" in {
          val apiPartialEntity = deserialize(jsonWithEndDate("null"))
          assert(apiPartialEntity.endDate.isAbsent)
        }
      }

      "when unspecified - should be parsed as Unspecified" in {
        val apiPartialEntity = deserialize(jsonWithoutEndDate)
        assert(apiPartialEntity.endDate.isUnspecified)
      }
    }

    "assignee" - {
      def jsonWithAssignee(rawValue: String) = json("assignee" -> rawValue)
      def jsonWithoutAssignee = json()

      "when specified" - {
        "if non-empty object - should be parsed as UUID" in {
          val rawAssignee = 123
          val apiPartialEntity = deserialize(jsonWithAssignee(quoted(rawAssignee.toString)))
          assert(apiPartialEntity.assignee.exists(_ == rawAssignee))
        }

        "if null - should be parsed as Absent" in {
          val apiPartialEntity = deserialize(jsonWithAssignee("null"))
          assert(apiPartialEntity.assignee.isAbsent)
        }
      }

      "when unspecified - should be parsed as Unspecified" in {
        val apiPartialEntity = deserialize(jsonWithoutAssignee)
        assert(apiPartialEntity.assignee.isUnspecified)
      }
    }

    "meta" - {
      def jsonWithMeta(rawValue: String) = json("meta" -> rawValue)
      def jsonWithoutMeta = json()

      "when specified" - {
        "if non-empty object - should be parsed as normalized JSON-string" in {
          val apiPartialEntity = deserialize(jsonWithMeta("""  { "foo" :  1} """))
          assert(apiPartialEntity.meta.exists(_ == """{"foo":1}"""))
        }

        "if null - should be parsed as Absent" in {
          val apiPartialEntity = deserialize(jsonWithMeta("null"))
          assert(apiPartialEntity.meta.isAbsent)
        }

        "if empty object ({}) - should be parsed as Absent (same as null case)" in {
          val apiPartialEntity = deserialize(jsonWithMeta(" {  } "))
          assert(apiPartialEntity.meta.isAbsent)
        }
      }

      "when unspecified - should be parsed as Unspecified" in {
        val apiPartialEntity = deserialize(jsonWithoutMeta)
        assert(apiPartialEntity.meta.isUnspecified)
      }
    }

    def deserialize(json: String): ApiPartialDocument = {
      Json.parse(json).as[ApiPartialDocument]
    }
  }

  "applyTo" - {
    "endDate" - {
      "should choose a value of the api partial document" - {
        "if it present" in test(Tristate.Present(LocalDateTime.now().plusDays(1)))
        "if it absent" in test(Tristate.Absent)

        def test(newEndDate: Tristate[LocalDateTime]): Unit = {
          val origDocument = documentExample
          val apiPartialDocument = apiPartialDocumentExample.copy(endDate = newEndDate)

          val newDocument = apiPartialDocument.applyTo(origDocument)
          assert(newDocument.endDate === newEndDate.toOption)
        }
      }

      "should choose a value of the original document if it unspecified" in {
        val newEndDate = Tristate.Unspecified
        val origDocument = documentExample
        val apiPartialDocument = apiPartialDocumentExample.copy(endDate = newEndDate)

        val newDocument = apiPartialDocument.applyTo(origDocument)
        assert(newDocument.endDate == origDocument.endDate)
      }
    }

    "assignee" - {
      "should choose a value of the api partial document" - {
        "if it present" in test(Tristate.Present(123))
        "if it absent" in test(Tristate.Absent)

        def test(newAssignee: Tristate[Long]): Unit = {
          val origDocument = documentExample
          val apiPartialDocument = apiPartialDocumentExample.copy(assignee = newAssignee)

          val newDocument = apiPartialDocument.applyTo(origDocument)
          assert(newDocument.assignee === newAssignee.toOption.map(LongId[User]))
        }
      }

      "should choose a value of the original document if it unspecified" in {
        val newAssignee = Tristate.Unspecified
        val origDocument = documentExample
        val apiPartialDocument = apiPartialDocumentExample.copy(assignee = newAssignee)

        val newDocument = apiPartialDocument.applyTo(origDocument)
        assert(newDocument.assignee == origDocument.assignee)
      }
    }

    /*"meta" - {
      val documentId = Id[Document]()
      def test(origMeta: Option[RawMeta], metaUpdate: Tristate[String], expectedNewMeta: Option[RawMeta]): Unit = {
        val apiPartialDocument = apiPartialDocumentExample.copy(meta = metaUpdate)

        val actualNewMeta = apiPartialDocument.applyTo(documentId, origMeta)
        assert(actualNewMeta == expectedNewMeta)
      }

      "should choose a new meta's value" - {
        "if it present" - {
          "when original meta is present" in test(
            origMeta = Some(RawMeta(documentId, """{"foo":42}""")),
            metaUpdate = Tristate.Present("""{"bar":1}"""),
            expectedNewMeta = Some(RawMeta(documentId, """{"bar":1}"""))
          )

          "when original meta is absent" in test(
            origMeta = None,
            metaUpdate = Tristate.Present("""{"bar":1}"""),
            expectedNewMeta = Some(RawMeta(documentId, """{"bar":1}"""))
          )
        }

        "if it absent" - {
          "when original meta is present" in test(
            origMeta = Some(RawMeta(documentId, """{"foo":42}""")),
            metaUpdate = Tristate.Absent,
            expectedNewMeta = None
          )

          "when original meta is absent" in test(
            origMeta = None,
            metaUpdate = Tristate.Absent,
            expectedNewMeta = None
          )
        }
      }

      "should choose an original meta if a new one is unspecified" - {
        "when original meta is present" in test(
          origMeta = Some(RawMeta(documentId, """{"foo":42}""")),
          metaUpdate = Tristate.Unspecified,
          expectedNewMeta = Some(RawMeta(documentId, """{"foo":42}"""))
        )

        "when original meta is absent" in test(
          origMeta = None,
          metaUpdate = Tristate.Unspecified,
          expectedNewMeta = None
        )
      }
    }*/
  }

  private def documentExample = Document(
    id = LongId(1),
    status = Document.Status.Organized,
    assignee = Some(LongId(1)),
    previousAssignee = None,
    recordId = LongId(1),
    physician = None,
    documentType = "type",
    providerName = "provider name",
    providerType = "provider type",
    meta = None,
    startDate = LocalDateTime.now,
    endDate = None,
    lastUpdate = LocalDateTime.now
  )

  private def apiPartialDocumentExample = ApiPartialDocument(
    recordId = Some(12),
    physician = Some("new physician"),
    `type` = Some("new type"),
    startDate = Some(LocalDateTime.now),
    endDate = Tristate.Present(LocalDateTime.now),
    provider = Some("new provider name"),
    providerType = Some("new provider type"),
    status = Some("Extracted"),
    assignee = Tristate.Present(13),
    meta = Tristate.Present("""{"foo":42}""")
  )

  private def quoted(v: String): String = s""""$v""""

  private def defaultJson = json(
    "endDate" -> quoted("2007-12-03T10:15:30.000"),
    "assignee" -> quoted("c8b1b818-e1eb-4831-b0c8-26753b109deb"),
    "meta" -> """{"foo": 1}"""
  )

  /**
    * Without fields:
    * - endDate
    * - assignee
    * - meta
    */
  private def json(addFields: (String, String)*): String = {
    val addJson = addFields
      .collect {
        case (k, v) if v.nonEmpty => s""" "$k": $v """
      }
      .mkString(",\n")

    s"""{
        |"recordId":"bc542d3d-a928-42b8-824d-ca1520b07500",
        |"patientId":"foobar",
        |"caseId":"bazquux",
        |"physician":"zed",
        |"lastUpdate":"2007-12-03T10:15:30.000",
        |"type":"type1",
        |"startDate":"2007-10-01T09:40:00.000",
        |"provider":"some provider name",
        |"providerType":"some provider type",
        |"status":"New"${if (addJson.isEmpty) "" else ","}
        |$addJson
        |}""".stripMargin
  }
}
 */