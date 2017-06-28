package xyz.driver.pdsuicommon.json

import xyz.driver.pdsuicommon.validation.JsonValidationErrors

class JsonValidationException(val errors: JsonValidationErrors) extends Exception
