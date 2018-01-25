package xyz.driver.restquery.http.parsers

class ParseQueryArgException(val errors: (String, String)*) extends Exception(errors.mkString(","))
