package services

import exceptions.{InvalidFromToException, SqlException}
import javax.inject.Inject
import models.DistanceResponses.{DistanceResponse, DurationResponse}
import models.Travel
import repositories.TravelsRepository
import utils.Logger

import scala.concurrent.{ExecutionContext, Future}

/**
  * Retrieves data from database and uses distance calculator to get minimal kilometers / duration between cities.
  */
class TraveledDistanceService @Inject() (travelsRepository: TravelsRepository)
                                        (implicit executionContext: ExecutionContext)
  extends Logger
{
  def findMinKilometers(from: String, to: String): Future[DistanceResponse] = {
    val fromSanitized = sanitize(from)
    val toSanitized = sanitize(to)
    logger.info(s"Finding min distance from $fromSanitized to $toSanitized")

    getTravelsSorted(fromSanitized, toSanitized, s"Error finding min distance from $fromSanitized to $toSanitized")
      .map { travels =>
        val distance = DistanceCalculator.findMinKilometers(travels, fromSanitized, toSanitized)
        DistanceResponse(distance.isDefined, distance)
      }
  }

  def findMinDuration(from: String, to: String): Future[DurationResponse] = {
    val fromSanitized = sanitize(from)
    val toSanitized = sanitize(to)
    logger.info(s"Finding min duration between $fromSanitized and $toSanitized")

    getTravelsSorted(fromSanitized, toSanitized, s"Error finding min duration between $fromSanitized and $toSanitized")
      .map { travels =>
        val duration = DistanceCalculator.findMinDuration(travels, fromSanitized, toSanitized)
        DurationResponse(duration.isDefined, duration)
      }
  }

  /**
    * Shared method to retrieve data for calculation from database. Handles problems with bad parameters or
    * crashed SQL request.
    */
  private def getTravelsSorted(from: String, to: String, errorMsg: => String): Future[Seq[Travel]] = {
    if (from.nonEmpty && to.nonEmpty) {
      travelsRepository.getTravelsSorted(from, to).recoverWith {
        case e: Throwable =>
          logger.error(s"$errorMsg: Error querying the database: ${e.getMessage}", e)
          Future.failed(SqlException(e))
      }
    } else {
      Future.failed(InvalidFromToException(from, to))
    }
  }

  private def sanitize(str: String) = str.trim.toLowerCase()
}
