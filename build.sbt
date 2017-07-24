import sbt._
import Keys._

lazy val core = (project in file("."))
  .driverLibrary("pds-ui-common")
  .settings(scalastyleSettings ++ wartRemoverSettings ++ formatSettings)
  .settings(wartremoverErrors in (Compile, compile) --= Seq(
    Wart.ImplicitConversion, Wart.MutableDataStructures, Wart.TraversableOps, Wart.OptionPartial))
  .settings(sources in (Compile, doc) := Seq.empty, publishArtifact in (Compile, packageDoc) := false)
  .settings(libraryDependencies ++= Seq(
    "com.fasterxml.jackson.module"      %% "jackson-module-scala"   % "2.8.3",
    "com.github.pureconfig"             %% "pureconfig"             % "0.7.2",
    "com.typesafe.akka"                 %% "akka-http"              % "10.0.9",
    "com.typesafe.play"                 %% "play"                   % "2.5.15",
    "com.typesafe.scala-logging"        %% "scala-logging"          % "3.5.0",
    "io.getquill"                       %% "quill-jdbc"             % "1.2.1",
    "io.github.cloudify"                %% "spdf"                   % "1.4.0",
    "org.davidbild"                     %% "tristate-core"          % "0.2.0",
    "org.davidbild"                     %% "tristate-play"          % "0.2.0" exclude ("com.typesafe.play", "play-json"),
    "xyz.driver"                        %% "core"                   % "0.14.0",
    "xyz.driver"                        %% "domain-model"           % "0.11.5",
    "ch.qos.logback"                    % "logback-classic"         % "1.1.7",
    "com.fasterxml.jackson.datatype"    % "jackson-datatype-jsr310" % "2.8.4",
    "com.github.spullara.mustache.java" % "scala-extensions-2.11"   % "0.9.4",
    "com.google.cloud"                  % "google-cloud-storage"    % "1.2.1",
    "com.sendgrid"                      % "sendgrid-java"           % "3.1.0" exclude ("org.mockito", "mockito-core"),
    "com.typesafe"                      % "config"                  % "1.3.0",
    "org.asynchttpclient"               % "async-http-client"       % "2.0.24",
    "org.slf4j"                         % "slf4j-api"               % "1.7.21",
    "ai.x"                              %% "diff"                   % "1.2.0-get-simple-name-fix" % "test",
    "org.scalatest"                     %% "scalatest"              % "3.0.0" % "test"
  ))
