import Dependencies._
import sbt.Keys.{ scalacOptions, _ }
import sbt._
import sbt.util.Level
import sbtassembly.AssemblyPlugin.autoImport.assembly
import sbtassembly.{AssemblyKeys, MergeStrategy}
import sbtassembly.PathList

object Settings {
    val ScalaVersion = "2.12.8"

    val commonSettings = {
        Seq(
            organization := "badilovskii.kh",
            scalaVersion := ScalaVersion,
            scalacOptions := Seq(
                "-deprecation",                      // Emit warning and location for usages of deprecated APIs.
                "-encoding", "utf-8",                // Specify character encoding used by source files.
                "-explaintypes",                     // Explain type errors in more detail.
                "-feature",                          // Emit warning and location for usages of features that should be imported explicitly.
                "-unchecked",                        // Enable additional warnings where generated code depends on assumptions.
                "-language:postfixOps",              // Enable postfix operations
                "-Xcheckinit",                       // Wrap field accessors to throw an exception on uninitialized access.
                "-Xlint:adapted-args",               // Warn if an argument list is modified to match the receiver.
                "-Xlint:constant",                   // Evaluation of a constant arithmetic expression results in an error.
                "-Xlint:inaccessible",               // Warn about inaccessible types in method signatures.
                "-Xlint:infer-any",                  // Warn when a type argument is inferred to be `Any`.
                "-Xlint:missing-interpolator",       // A string literal appears to be missing an interpolator id.
                "-Xlint:nullary-override",           // Warn when non-nullary `def f()' overrides nullary `def f'.
                "-Xlint:package-object-classes",     // Class or object defined in package object.
                "-Xlint:poly-implicit-overload",     // Parameterized overloaded implicit methods are not visible as view bounds.
                "-Xlint:private-shadow",             // A private field (or class parameter) shadows a superclass field.
                "-Xlint:stars-align",                // Pattern sequence wildcard must align with sequence component.
                "-Xlint:type-parameter-shadow"       // A local type parameter shadows a type already in scope.
            ),
            javacOptions ++= Seq("-g", "-source", "1.8", "-target", "1.8", "-encoding", "UTF-8"),
            logLevel := Level.Info,
            version := (version in ThisBuild).value,  resolvers += Resolver.jcenterRepo
        ) ++ Seq(mainClass in assembly := Some("com.badilovskii.kh.Main"),
            AssemblyKeys.assemblyMergeStrategy in assembly := {
                case PathList("META-INF", xs @ _*) => MergeStrategy.discard
                case PathList("reference.conf") => MergeStrategy.concat
                case x => MergeStrategy.first
            }
        )
    }

    val commonDependencies = Seq(logback, logstash, slf4j, scalaLogging, scalaTest % Test, mockito % Test)

    val serviceDependencies = Seq(akkaStream, akkaPersistence, akkaPersistenceQuery, levelDB, levelDBjni, akkaTestKit % Test) ++ commonDependencies

    val apiDependencies = Seq(akkaHttp, akkaHttpCors, akkaHttpTestKit % Test, akkaStreamTestKit % Test) ++ circe ++ tapir

    val bootDependencies = Seq(pureConfig)
}
