package controllers

import javax.inject.Inject
import play.api.libs.json.Json
import play.api.mvc.{AbstractController, ControllerComponents}
import services.{TraveledDistanceService, TravelsChartService}

import scala.concurrent.ExecutionContext

class TravelChartController @Inject()(cc: ControllerComponents,
                                      travelsChartService: TravelsChartService
                                     )(implicit ec: ExecutionContext)
  extends AbstractController(cc)
{
  def getDailyChart() = Action.async { implicit r =>
    travelsChartService.getDailyChart().map(v => Ok(Json.toJson(v)))
  }

  def getWeeklyChart() = Action.async { implicit r =>
    travelsChartService.getWeeklyChart().map(v => Ok(Json.toJson(v)))
  }
}
