package models

import play.api.libs.json.{Format, Json}

object DistanceResponses {
  case class DistanceResponse(pathExists: Boolean, kilometers: Option[Long])
  object DistanceResponse
  {
    implicit val format: Format[DistanceResponse] = Json.format[DistanceResponse]
  }

  case class DurationResponse(pathExists: Boolean, days: Option[Long])
  object DurationResponse
  {
    implicit val format: Format[DurationResponse] = Json.format[DurationResponse]
  }
}
