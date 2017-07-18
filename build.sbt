import sbt._
import Keys._

lazy val core = (project in file("."))
  .driverLibrary("pds-ui-common")
  .settings(scalastyleSettings ++ /* wartRemoverSettings ++ */ formatSettings)
  .settings(sources in (Compile, doc) := Seq.empty, publishArtifact in (Compile, packageDoc) := false)
  .settings(libraryDependencies ++= Seq(
    "ch.qos.logback"                    % "logback-classic"         % "1.1.7",
    "org.slf4j"                         % "slf4j-api"               % "1.7.21",
    "com.typesafe.scala-logging"        %% "scala-logging"          % "3.5.0",
    "com.typesafe"                      % "config"                  % "1.3.0",
    "com.fasterxml.jackson.module"      %% "jackson-module-scala"   % "2.8.3",
    "com.fasterxml.jackson.datatype"    % "jackson-datatype-jsr310" % "2.8.4",
    "com.typesafe.play"                 %% "play"                   % "2.5.15",
    "xyz.driver"                        %% "core"                   % "0.13.22",
    "xyz.driver"                        %% "domain-model"           % "0.11.5",
    "org.davidbild"                     %% "tristate-core"          % "0.2.0",
    "org.davidbild"                     %% "tristate-play"          % "0.2.0" exclude ("com.typesafe.play", "play-json"),
    "org.asynchttpclient"               % "async-http-client"       % "2.0.24",
    "io.getquill"                       %% "quill-jdbc"             % "1.2.1",
    "io.github.cloudify"                %% "spdf"                   % "1.4.0",
    "com.github.spullara.mustache.java" % "scala-extensions-2.11"   % "0.9.4",
    "com.google.cloud"                  % "google-cloud-storage"    % "0.9.4-beta",
    "com.github.pureconfig"             %% "pureconfig"             % "0.7.2",
    "ai.x"                              %% "diff"                   % "1.2.0-get-simple-name-fix" % "test",
    "org.scalatest"                     %% "scalatest"              % "3.0.0" % "test"
  ))
