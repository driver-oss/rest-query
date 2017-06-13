package xyz.driver.pdsuicommon.error

import xyz.driver.pdsuicommon.validation.ValidationError

class FailedValidationException(val error: ValidationError) extends RuntimeException("The validation is failed")
