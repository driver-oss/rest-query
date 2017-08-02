package xyz.driver.server.parsers.errors

class ParseQueryArgException(val errors: (String, String)*) extends Exception(errors.mkString(","))
