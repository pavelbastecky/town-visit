package models

import info.BuildInfo
import models.Version.Git
import play.api.libs.json.{Format, Json}

case class Version(name: String,
                   version: String,
                   scalaVersion: String,
                   sbtVersion: String,
                   builtAtString: String,
                   builtAtMillis: Long,
                   git: Git,
                  )

object Version {
  case class Git(commit: String, branch: String)

  def getInfo = Version(
    BuildInfo.name,
    BuildInfo.version,
    BuildInfo.scalaVersion,
    BuildInfo.sbtVersion,
    BuildInfo.builtAtString,
    BuildInfo.builtAtMillis,
    Git(
      BuildInfo.gitCommit,
      BuildInfo.gitBranch,
    ))

  implicit val gitFormat: Format[Git] = Json.format[Git]
  implicit val versionFormat: Format[Version] = Json.format[Version]
}

