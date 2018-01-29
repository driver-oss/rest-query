import sbt._
import Keys._

lazy val core = (project in file("."))
  .driverLibrary("rest-query")
  .settings(lintingSettings ++ formatSettings)
  .settings(sources in (Compile, doc) := Seq.empty, publishArtifact in (Compile, packageDoc) := false)
  .settings(libraryDependencies ++= Seq(
    "com.typesafe.akka"          %% "akka-http"      % "10.0.11",
    "com.lihaoyi"                %% "fastparse"      % "1.0.0",
    "xyz.driver"                 %% "core"           % "1.7.0",
    "com.typesafe.scala-logging" %% "scala-logging"  % "3.5.0",
    "org.slf4j"                  % "slf4j-api"       % "1.7.21",
    "ch.qos.logback"             % "logback-classic" % "1.1.7",
    "com.typesafe"               % "config"          % "1.3.1",
    "org.scalacheck"             %% "scalacheck"     % "1.13.4" % "test",
    "org.scalatest"              %% "scalatest"      % "3.0.2"  % "test"
  ))
