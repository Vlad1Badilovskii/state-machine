import sbt._
import Settings._

lazy val service = project
    .settings(libraryDependencies ++= serviceDependencies)
    .settings(commonSettings)

lazy val api = project
    .dependsOn(service % "compile->compile;test->test")
    .settings(libraryDependencies ++= apiDependencies)
    .settings(commonSettings)

lazy val boot = project
    .dependsOn(api % "compile->compile;test->test")
    .settings(libraryDependencies ++= bootDependencies)
    .settings(commonSettings)

lazy val `state-machine` = Project("root", file("."))
    .aggregate(service, boot, api)
    .settings(moduleName := "state-machine")
    .settings(name := "state-machine")
    .settings(commonSettings)

