import sbt._
import Keys._

lazy val akkaHttpV = "10.0.5"

lazy val core = (project in file("."))
  .driverLibrary("pds-ui-common")
  .settings(scalastyleSettings ++ /* wartRemoverSettings ++ */ formatSettings)
  .settings(libraryDependencies ++= Seq(
    "com.typesafe.akka"              %% "akka-http-core"         % akkaHttpV,
    "com.typesafe.akka"              %% "akka-http-spray-json"   % akkaHttpV,
    "com.typesafe.akka"              %% "akka-http-testkit"      % akkaHttpV,
    "org.davidbild"                  %% "tristate-core"          % "0.2.0",
    "org.davidbild"                  %% "tristate-play"          % "0.2.0" exclude ("com.typesafe.play", "play-json"),
    "org.asynchttpclient"            % "async-http-client"       % "2.0.24",
    "io.github.cloudify"             %% "spdf"                   % "1.4.0",
    "com.github.pureconfig"          %% "pureconfig"             % "0.7.2",
    "de.svenkubiak"                  % "jBCrypt"                 % "0.4.1",
    "com.fasterxml.jackson.datatype" % "jackson-datatype-jsr310" % "2.8.4",
    "org.scalatest"                  % "scalatest_2.11"          % "2.2.6" % "test",
    "org.scalacheck"                 %% "scalacheck"             % "1.12.5" % "test",
    "org.mockito"                    % "mockito-core"            % "1.9.5" % "test",
    "ai.x"                           %% "diff"                   % "1.2.0-get-simple-name-fix" % "test",
    "com.github.swagger-akka-http"   %% "swagger-akka-http"      % "0.9.1",
    "com.google.cloud"               % "google-cloud-storage"    % "0.9.4-beta" excludeAll (
      ExclusionRule(organization = "io.netty")
    ),
    "io.getquill"                %% "quill-jdbc"     % "1.2.1",
    "com.typesafe.slick"         %% "slick"          % "3.1.1",
    "com.typesafe"               % "config"          % "1.2.1",
    "com.typesafe.scala-logging" %% "scala-logging"  % "3.4.0",
    "ch.qos.logback"             % "logback-classic" % "1.1.3"
  ))
