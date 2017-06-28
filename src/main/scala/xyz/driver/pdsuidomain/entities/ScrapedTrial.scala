package xyz.driver.pdsuidomain.entities

import java.time.LocalDateTime
import java.util.UUID

import xyz.driver.pdsuicommon.logging._
import xyz.driver.pdsuicommon.domain.UuidId

final case class ScrapedStudyDesign(value: String)

final case class ScrapedOverall(affiliation: String,
                                status: String,
                                facilityName: Option[String],
                                firstName: Option[String],
                                lastName: Option[String],
                                phone: Option[String],
                                phoneExt: Option[String],
                                email: Option[String],
                                isBackup: Boolean)

final case class ScrapedLocationContact(firstName: Option[String],
                                        lastName: Option[String],
                                        phone: Option[String],
                                        phoneExt: Option[String],
                                        email: Option[String],
                                        isBackup: Boolean)

final case class ScrapedLocation(id: UuidId[ScrapedLocation],
                                 createdAt: LocalDateTime,
                                 facilityName: Option[String],
                                 city: Option[String],
                                 state: Option[String],
                                 zip: Option[String],
                                 country: Option[String],
                                 latitude: Option[Double],
                                 longitude: Option[Double],
                                 preferredName: Option[String],
                                 partnershipStatus: Option[String],
                                 lastReviewed: LocalDateTime,
                                 contacts: Set[ScrapedLocationContact])

final case class ScrapedInterventionType(value: String)

final case class ScrapedIntervention(name: String,
                                     kind: ScrapedInterventionType,
                                     description: Option[String],
                                     isSynonym: Boolean)

object ScrapedIntervention {

  implicit def toPhiString(x: ScrapedIntervention): PhiString = phi"ScrapedIntervention(${Unsafe(x.name)})"
}

final case class ScrapedArm(name: String,
                            kind: Option[String],
                            interventions: Set[ScrapedIntervention])

object ScrapedArm {

  implicit def toPhiString(x: ScrapedArm): PhiString = {
    import x._
    phi"ScrapedArm(name=${Unsafe(name)}, inverventions=$interventions)"
  }
}

final case class ScrapedTrialChecksum(eligibilityCriteria: String,
                                      briefSummary: String,
                                      detailedDescription: String,
                                      armDescription: String)

object ScrapedTrialChecksum {

  implicit def toPhiString(x: ScrapedTrialChecksum): PhiString = {
    import x._
    phi"ScrapedTrialChecksum(eligibilityCriteria=${Unsafe(eligibilityCriteria)}, briefSummary=${Unsafe(briefSummary)}, " +
      phi"detailedDescription=${Unsafe(detailedDescription)}, armDescription=${Unsafe(armDescription)}"
  }
}

object ScrapedTrial {

  implicit def toPhiString(x: ScrapedTrial): PhiString = {
    import x._
    phi"ScrapedTrial(rawId=$rawId, nctId=${Unsafe(nctId)}, " +
      phi"location.size=${Unsafe(locations.size)}, arms=$arms, checksum=$checksum)"
  }
}

final case class ScrapedTrial(rawId: UuidId[ScrapedTrial],
                              createdAt: LocalDateTime,
                              disease: String,
                              nctId: String,
                              nctUuid: UUID,
                              title: Option[String],
                              startDate: Option[LocalDateTime],
                              phase: String,
                              studyDesign: Option[ScrapedStudyDesign],
                              overall: Set[ScrapedOverall],
                              locations: Set[ScrapedLocation],
                              // // see ClinicalTrialRaw
                              // trialHtml: String,
                              // eligibilityText: String,
                              lastReviewed: LocalDateTime,
                              arms: Set[ScrapedArm],
                              checksum: ScrapedTrialChecksum)
