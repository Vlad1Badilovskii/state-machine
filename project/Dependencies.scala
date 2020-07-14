import sbt._
import sbt.librarymanagement.syntax.ExclusionRule

object Dependencies {
    val akkaStream = "com.typesafe.akka" %% "akka-stream" % Version.akka
    val akkaHttp = "com.typesafe.akka" %% "akka-http" % Version.akkaHttp
    val akkaPersistence = "com.typesafe.akka" %% "akka-persistence" % Version.akka
    val akkaPersistenceQuery = "com.typesafe.akka" %% "akka-persistence-query" % Version.akka

    val logback = "ch.qos.logback" % "logback-classic" % Version.logback
    val logstash = "net.logstash.logback" % "logstash-logback-encoder" % Version.logstash
    val slf4j = "com.typesafe.akka" %% "akka-slf4j" % Version.akka

    val levelDB =  "org.iq80.leveldb" % "leveldb" % Version.levelDB
    val levelDBjni =  "org.fusesource.leveldbjni" % "leveldbjni-all" % Version.levelDBjni

    val akkaHttpCors = "ch.megard" %% "akka-http-cors" % Version.akkaHttpCors

    val tapirCore = "com.softwaremill.tapir" %% "tapir-core" % Version.tapir
    val tapirJson = "com.softwaremill.tapir" %% "tapir-json-circe" % Version.tapir
    val tapirAkkaHttp = "com.softwaremill.tapir" %% "tapir-akka-http-server" % Version.tapir
    val tapirOpenApi = "com.softwaremill.tapir" %% "tapir-openapi-docs" % Version.tapir
    val tapirCirceYaml = "com.softwaremill.tapir" %% "tapir-openapi-circe-yaml" % Version.tapir
    val tapirSwagger = "com.softwaremill.tapir" %% "tapir-swagger-ui-akka-http" % Version.tapir excludeAll (
        ExclusionRule().withOrganization("com.typesafe.akka").withName("akka-http_2.13"),
        ExclusionRule().withOrganization("com.typesafe.akka").withName("akka-actor:2.6.3"),
    )
    val tapir = List(tapirCore, tapirJson, tapirAkkaHttp, tapirOpenApi, tapirCirceYaml, tapirSwagger)

    val circeCore = "io.circe" %% "circe-core" % Version.circe
    val circeGeneric = "io.circe" %% "circe-generic" % Version.circe
    val circeParser = "io.circe" %% "circe-parser" % Version.circe
    val akkaHttpCirce = "de.heikoseeberger" %% "akka-http-circe" % Version.akkaHttpCirce
    val circe = List(circeCore, circeGeneric, circeParser, akkaHttpCirce)

    val scalaLogging = "com.typesafe.scala-logging" %% "scala-logging" % Version.scalaLogging

    val mockito = "org.mockito" %% "mockito-scala" % Version.mockito
    val scalaTest = "org.scalatest" %% "scalatest" % Version.scalaTest
    val akkaHttpTestKit = "com.typesafe.akka" %% "akka-http-testkit" % Version.akkaHttp
    val akkaStreamTestKit = "com.typesafe.akka" %% "akka-stream-testkit" % Version.akka

    val akkaTestKit = "com.typesafe.akka" %% "akka-testkit" % Version.akka

    val pureConfig = "com.github.pureconfig" %% "pureconfig" % Version.pureConfig
}

object Version {
    val akka = "2.5.31"
    val akkaHttp = "10.1.10"
    val logback = "1.2.3"
    val logstash = "4.11"
    val scalaTest = "3.1.1"
    val mockito = "1.5.16"
    val slf4j = "1.7.25"
    val pureConfig = "0.11.1"
    val scalaLogging = "3.9.2"
    val levelDB = "0.11"
    val levelDBjni = "1.8"
    val circe = "0.12.3"
    val akkaHttpCirce = "1.33.0"
    val tapir = "0.11.11"
    val akkaHttpCors = "0.4.2"
}
