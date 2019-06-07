package models

import models.WeeklyChart.ChartItem
import play.api.libs.json.{Format, Json}

case class WeeklyChart(items: Seq[ChartItem])

object WeeklyChart {
  case class ChartItem(year: Int, week: Int, traveled: Int)

  implicit val itemFormat: Format[ChartItem] = Json.format[ChartItem]

  implicit val format: Format[WeeklyChart] = Json.format[WeeklyChart]
}


