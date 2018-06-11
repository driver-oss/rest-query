import sbt._
import Keys._

lazy val core = (project in file("."))
  .driverLibrary("rest-query")
  .settings(lintingSettings ++ formatSettings)
  .settings(sources in (Compile, doc) := Seq.empty, publishArtifact in (Compile, packageDoc) := false)
  .settings(libraryDependencies ++= Seq(
    "com.lihaoyi"                %% "fastparse"      % "1.0.0",
    "xyz.driver"                 %% "core"           % "1.10.1",
    "org.scalacheck"             %% "scalacheck"     % "1.13.4" % "test",
    "org.scalatest"              %% "scalatest"      % "3.0.5"  % "test"
  ))
