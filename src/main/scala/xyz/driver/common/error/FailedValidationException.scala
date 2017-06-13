package xyz.driver.common.error

import xyz.driver.common.validation.ValidationError

class FailedValidationException(val error: ValidationError) extends RuntimeException("The validation is failed")
