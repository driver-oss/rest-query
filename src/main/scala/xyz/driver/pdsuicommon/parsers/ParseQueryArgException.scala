package xyz.driver.pdsuicommon.parsers

class ParseQueryArgException(val errors: (String, String)*) extends Exception(errors.mkString(","))
