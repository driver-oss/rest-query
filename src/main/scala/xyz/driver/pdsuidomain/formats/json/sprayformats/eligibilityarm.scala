package xyz.driver.pdsuidomain.formats.json.sprayformats

import spray.json._
import xyz.driver.entities.patient.CancerType
import xyz.driver.formats.json.patient._
import xyz.driver.pdsuicommon.domain.{LongId, StringId}
import xyz.driver.pdsuidomain.entities._

object eligibilityarm {

  import DefaultJsonProtocol._
  import common._

  private def deserializationErrorFieldMessage(field: String, json: JsValue)(implicit className: String) = {
    deserializationError(s"$className json object do not contain '$field' field: $json")
  }

  private def deserializationErrorEntityMessage(json: JsValue)(implicit className: String) = {
    deserializationError(s"Expected Json Object as $className, but got $json")
  }


  implicit def eligibilityArmWithDiseasesWriter: RootJsonWriter[EligibilityArmWithDiseases] =
    new RootJsonWriter[EligibilityArmWithDiseases] {
      override def write(obj: EligibilityArmWithDiseases): JsValue = {
        JsObject(
          "id" -> obj.eligibilityArm.id.toJson,
          "name" -> obj.eligibilityArm.name.toJson,
          "originalName" -> obj.eligibilityArm.originalName.toJson,
          "trialId" -> obj.eligibilityArm.trialId.toJson,
          "diseases" -> obj.eligibilityArmDiseases.map(_.disease.toJson).toJson
        )
      }
    }


  implicit def eligibilityArmWithDiseasesReader: RootJsonReader[EligibilityArmWithDiseases] = {
    new RootJsonReader[EligibilityArmWithDiseases] {
      implicit val className: String = "create EligibilityArmWithDiseases"

      override def read(json: JsValue): EligibilityArmWithDiseases = {
        json match {
          case JsObject(fields) =>
            val name = fields
              .get("name")
              .map(_.convertTo[String])
              .getOrElse(deserializationErrorFieldMessage("name", json))

            val trialId = fields
              .get("trialId")
              .map(_.convertTo[StringId[Trial]])
              .getOrElse(deserializationErrorFieldMessage("trialId", json))

            val diseases = fields
              .get("diseases")
              .map(_.convertTo[Seq[String]])
              .getOrElse(deserializationErrorFieldMessage("diseases", json))



            val eligibilityArm = EligibilityArm(
              id = LongId(0),
              name = name,
              trialId = trialId,
              originalName = name
            )

            EligibilityArmWithDiseases(
              eligibilityArm,
              diseases.map { disease =>
                val condition = CancerType
                  .fromString(disease)
                  .getOrElse(throw new NoSuchElementException(s"unknown condition $disease"))
                EligibilityArmDisease(eligibilityArm.id, condition)
              }
            )
          case _ => deserializationErrorEntityMessage(json)
        }
      }
    }
  }

  def applyUpdateToEligibilityArmWithDiseases(json: JsValue, orig: EligibilityArmWithDiseases): EligibilityArmWithDiseases = {
    implicit val className: String = "update EligibilityArmWithDiseases"
    json match {
      case JsObject(fields) =>
        val name = fields
          .get("name")
          .map(_.convertTo[String])
          .getOrElse(orig.eligibilityArm.name)

        val diseases = fields
          .get("diseases")
          .map(_.convertTo[Seq[CancerType]])
          .getOrElse(orig.eligibilityArmDiseases.map(_.disease))

        orig.copy(
          eligibilityArm =
            orig
              .eligibilityArm
              .copy(name = name),
          eligibilityArmDiseases =
            orig
              .eligibilityArmDiseases
              .zip(diseases)
              .map {
                case (eligibilityArmDisease, disease) =>
                  eligibilityArmDisease.copy(disease = disease)
              }
        )

      case _ => deserializationErrorEntityMessage(json)
    }
  }
}
