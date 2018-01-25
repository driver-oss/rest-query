package xyz.driver.restquery.rest.parsers

class ParseQueryArgException(val errors: (String, String)*) extends Exception(errors.mkString(","))
