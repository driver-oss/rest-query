package xyz.driver.pdsuidomain.formats.json.sprayformats

import java.time.{LocalDate, LocalDateTime}

import spray.json._
import org.scalatest.{FlatSpec, Matchers}
import xyz.driver.entities.common.FullName
import xyz.driver.entities.patient.CancerType
import xyz.driver.pdsuicommon.domain.UuidId
import xyz.driver.pdsuidomain.entities.{Patient, PatientOrderId}

class PatientFormatSuite extends FlatSpec with Matchers {
  import patient._

  "Json format for Patient" should "read and write correct JSON" in {
    val orig = Patient(
      id = UuidId("748b5884-3528-4cb9-904b-7a8151d6e343"),
      status = Patient.Status.New,
      name = FullName.fromStrings("John", "", "Doe"),
      dob = LocalDate.parse("1980-06-30"),
      assignee = None,
      previousStatus = None,
      previousAssignee = None,
      lastActiveUserId = None,
      isUpdateRequired = false,
      cancerType = CancerType.Breast,
      orderId = PatientOrderId("7b54a75d-4197-4b27-9045-b9b6cb131be9"),
      lastUpdate = LocalDateTime.parse("2017-08-10T18:00:00")
    )
    val writtenJson = patientWriter.write(orig)

    writtenJson should be (
      """{"id":"748b5884-3528-4cb9-904b-7a8151d6e343","dob":"1980-06-30",
         "name":{"firstName":"John","middleName":"","lastName":"Doe"},"status":"New","assignee":null,
         "previousStatus":null,"previousAssignee":null,"lastActiveUser":null,"lastUpdate":"2017-08-10T18:00Z",
         "orderId":"7b54a75d-4197-4b27-9045-b9b6cb131be9","condition":"Breast"}""".parseJson)
  }

}
