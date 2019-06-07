package services

import java.time.LocalDate

import models.{DailyChart, Travel, WeeklyChart}
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerTest
import play.api.test.{DefaultAwaitTimeout, FutureAwaits}
import repositories.TravelsRepository

class TravelsChartServiceTest   extends PlaySpec
  with GuiceOneAppPerTest
  with FutureAwaits
  with DefaultAwaitTimeout
{
  def getTravel(city: String, date: String, kilometers: Int, total: Long) =
    Travel(0, LocalDate.parse(date), city, kilometers, Some(total))

  private val cities = Seq(
    getTravel("Praha", "2019-05-01", 0, 0),
    getTravel("Beroun", "2019-05-05", 32, 32),
    getTravel("Praha", "2019-05-06", 32, 64),
    getTravel("Beroun", "2019-05-06", 32, 96),
    getTravel("Praha", "2019-05-06", 32, 128),
    getTravel("Ostrava", "2019-05-15", 370, 498),
    getTravel("Brno", "2019-05-15", 170, 668),
    getTravel("Praha", "2019-05-16", 200, 868),
    getTravel("Olomouc", "2019-05-17", 280, 1148),
    getTravel("Brno", "2019-05-18", 80, 1228),
  )

  "TraveledDistanceServiceTest" should {
    "aggregate travels for daily chart" in {
      val repo = app.injector.instanceOf[TravelsRepository]
      await(repo.truncate())
      await(repo.insertAll(cities))

      val service = app.injector.instanceOf[TravelsChartService]

      await(service.getDailyChart()) mustBe DailyChart(
        Seq(
          DailyChart.ChartItem(LocalDate.parse("2019-05-01"), 0),
          DailyChart.ChartItem(LocalDate.parse("2019-05-05"), 32),
          DailyChart.ChartItem(LocalDate.parse("2019-05-06"), 96),
          DailyChart.ChartItem(LocalDate.parse("2019-05-15"), 370 + 170),
          DailyChart.ChartItem(LocalDate.parse("2019-05-16"), 200),
          DailyChart.ChartItem(LocalDate.parse("2019-05-17"), 280),
          DailyChart.ChartItem(LocalDate.parse("2019-05-18"), 80),
        )
      )
    }

    "aggregate travels for weekly chart" in {
      val repo = app.injector.instanceOf[TravelsRepository]
      await(repo.truncate())
      await(repo.insertAll(cities))

      val service = app.injector.instanceOf[TravelsChartService]

      await(service.getWeeklyChart()) mustBe WeeklyChart(
        Seq(
          WeeklyChart.ChartItem(2019, 18, 0),
          WeeklyChart.ChartItem(2019, 19, 128),
          WeeklyChart.ChartItem(2019, 20, 1100),
        )
      )
    }
  }
}
