package xyz.driver.pdsuicommon

import play.api.data.validation.{ValidationError => PlayValidationError}
import play.api.libs.json.JsPath

package object validation {
  type JsonValidationErrors = Seq[(JsPath, Seq[PlayValidationError])]
}
