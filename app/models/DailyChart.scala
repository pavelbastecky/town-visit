package models

import java.time.LocalDate

import models.DailyChart.ChartItem
import play.api.libs.json.{Format, Json}

case class DailyChart(items: Seq[ChartItem])

object DailyChart {
  case class ChartItem(date: LocalDate, traveled: Int)

  implicit val itemFormat: Format[ChartItem] = Json.format[ChartItem]

  implicit val format: Format[DailyChart] = Json.format[DailyChart]
}
