import sbt.Keys._

lazy val GatlingTest = config("gatling") extend Test

scalaVersion in ThisBuild := "2.12.8"

libraryDependencies += guice
libraryDependencies += "org.joda" % "joda-convert" % "2.1.2"
libraryDependencies += "net.logstash.logback" % "logstash-logback-encoder" % "5.2"

libraryDependencies += "com.netaporter" %% "scala-uri" % "0.4.16"
libraryDependencies += "net.codingwell" %% "scala-guice" % "4.2.1"

libraryDependencies += "postgresql" % "postgresql" % "9.1-901-1.jdbc4"
libraryDependencies += "org.flywaydb" %% "flyway-play" % "5.3.2"

libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "4.0.2" % Test
libraryDependencies += "io.gatling.highcharts" % "gatling-charts-highcharts" % "3.1.2" % Test
libraryDependencies += "io.gatling" % "gatling-test-framework" % "3.1.2" % Test

val gitCommitString = SettingKey[String]("gitCommit")
gitCommitString := git.gitHeadCommit.value.getOrElse("Not Set")

val gitBranchString = SettingKey[String]("gitBranch")
gitBranchString := git.gitCurrentBranch.value

// The Play project itself
lazy val root = (project in file("."))
  .enablePlugins(Common, PlayService, PlayLayoutPlugin, GatlingPlugin, BuildInfoPlugin, GitVersioning)
  .configs(GatlingTest)
  .settings(inConfig(GatlingTest)(Defaults.testSettings): _*)
  .settings(
    name := """town-visit""",
    scalaSource in GatlingTest := baseDirectory.value / "/gatling/simulation",

    buildInfoKeys := Seq[BuildInfoKey](
        name, version, scalaVersion, sbtVersion, gitCommitString, gitBranchString
    ),
    buildInfoPackage := "info",
    buildInfoOptions += BuildInfoOption.BuildTime,
  )
