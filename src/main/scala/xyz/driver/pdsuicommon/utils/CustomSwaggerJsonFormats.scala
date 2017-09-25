package xyz.driver.pdsuicommon.utils

import java.time.{LocalDate, LocalDateTime}

import io.swagger.models.properties.Property
import spray.json.JsValue
import xyz.driver.pdsuicommon.domain.{LongId, StringId, UuidId}
import xyz.driver.pdsuidomain.entities._
import xyz.driver.pdsuidomain.formats.json.sprayformats.arm._
import xyz.driver.pdsuidomain.formats.json.sprayformats.criterion._
import xyz.driver.pdsuidomain.formats.json.sprayformats.intervention._
import xyz.driver.pdsuidomain.formats.json.sprayformats.hypothesis._
import xyz.driver.pdsuidomain.formats.json.sprayformats.studydesign._
import xyz.driver.pdsuidomain.formats.json.sprayformats.trial._
import xyz.driver.pdsuidomain.formats.json.sprayformats.trialhistory._
import xyz.driver.pdsuidomain.formats.json.sprayformats.trialissue._
import xyz.driver.core.swagger.CustomSwaggerJsonConverter._
import xyz.driver.pdsuidomain.services.CriterionService.RichCriterion

object CustomSwaggerJsonFormats {

  val customCommonProperties = Map[Class[_], Property](
    classOf[LocalDateTime] -> stringProperty(example = Some("2010-12-31'T'18:59:59Z")),
    classOf[LocalDate]     -> stringProperty(example = Some("2010-12-31")),
    classOf[UuidId[_]]     -> stringProperty(example = Some("370b0450-35cb-4aab-ba74-0145be75add5")),
    classOf[StringId[_]]   -> stringProperty(),
    classOf[LongId[_]]     -> stringProperty()
  )
  val customTrialCurationProperties = Map[Class[_], Property](
    classOf[Trial.Status]        -> stringProperty(),
    classOf[Trial.Condition]     -> stringProperty(),
    classOf[TrialHistory.Action] -> stringProperty(),
    classOf[TrialHistory.State]  -> stringProperty()
  ) ++ customCommonProperties

  val customTrialCurationObjectsExamples = Map[Class[_], JsValue](
    classOf[Trial] -> trialWriter.write(xyz.driver.pdsuidomain.fakes.entities.trialcuration.nextTrial()),
    classOf[Arm]   -> armFormat.write(xyz.driver.pdsuidomain.fakes.entities.trialcuration.nextArm()),
    classOf[TrialHistory] -> trialHistoryFormat.write(
      xyz.driver.pdsuidomain.fakes.entities.trialcuration.nextTrialHistory()),
    classOf[TrialIssue] -> trialIssueWriter.write(
      xyz.driver.pdsuidomain.fakes.entities.trialcuration.nextTrialIssue()),
    classOf[RichCriterion] -> richCriterionFormat.write(
      xyz.driver.pdsuidomain.fakes.entities.trialcuration.nextRichCriterion()),
    classOf[InterventionWithArms] -> interventionFormat.write(
      xyz.driver.pdsuidomain.fakes.entities.trialcuration.nextInterventionWithArms()),
    classOf[InterventionType] -> interventionTypeFormat.write(
      xyz.driver.pdsuidomain.fakes.entities.trialcuration.nextInterventionType()),
    classOf[Hypothesis] -> hypothesisFormat.write(
      xyz.driver.pdsuidomain.fakes.entities.trialcuration.nextHypothesis()),
    classOf[StudyDesign] -> studyDesignFormat.write(
      xyz.driver.pdsuidomain.fakes.entities.trialcuration.nextStudyDesign())
  )

}
