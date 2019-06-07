package controllers

import javax.inject.Inject
import play.api.libs.json.Json
import play.api.mvc.{AbstractController, ControllerComponents}
import services.TraveledDistanceService

import scala.concurrent.ExecutionContext

class TraveledDistanceController @Inject()(cc: ControllerComponents,
                                           traveledDistanceService: TraveledDistanceService
                                          )(implicit ec: ExecutionContext)
  extends AbstractController(cc)
{
  def getTraveledDistance(from: String, to: String) = Action.async { implicit r =>
    traveledDistanceService.findMinKilometers(from, to).map(v => Ok(Json.toJson(v)))
  }

  def getTraveledDuration(from: String, to: String) = Action.async { implicit r =>
    traveledDistanceService.findMinDuration(from, to).map(v => Ok(Json.toJson(v)))
  }
}
