package services

import java.time.LocalDate
import java.time.temporal.WeekFields
import java.util.Locale

import exceptions.SqlException
import javax.inject.Inject
import models.{DailyChart, WeeklyChart}
import repositories.TravelsRepository
import utils.Logger

import scala.concurrent.{ExecutionContext, Future}

/**
  * Service for producing aggregated traveled distance in time periods.
  */
class TravelsChartService@Inject() (travelsRepository: TravelsRepository)
                                   (implicit executionContext: ExecutionContext)
  extends Logger
{
  private val weekFields = WeekFields.of(Locale.getDefault())

  def getDailyChart(): Future[DailyChart] = {
    logger.info("Getting daily chart")

    def toChartItems(items: Seq[(LocalDate, Option[Int])]) = {
      items.map(i => DailyChart.ChartItem(i._1, i._2.getOrElse(0)))
    }

    getDailyTravels("Error getting daily chart")
      .map(items => DailyChart(toChartItems(items)))
  }

  def getWeeklyChart(): Future[WeeklyChart] = {
    logger.info("Getting weekly chart")

    def toChartItems(items: Seq[(LocalDate, Option[Int])]) = {
      items
        .groupBy(i => (i._1.getYear, i._1.get(weekFields.weekOfWeekBasedYear())))
        .map { case ((year, week), kilometers) =>
          val travelsInWeek = kilometers.foldLeft(0)(_ + _._2.getOrElse(0))
          WeeklyChart.ChartItem(year, week, travelsInWeek)
        }.toSeq
    }

    getDailyTravels("Error getting weekly chart")
      .map(items => WeeklyChart(toChartItems(items)))
  }

  private def getDailyTravels(errMsg: String) = {
    travelsRepository.getDailyTravels()
      .recoverWith {
        case e: Throwable =>
          logger.error(s"$errMsg: Error querying the database: ${e.getMessage}", e)
          Future.failed(SqlException(e))
      }
  }
}
