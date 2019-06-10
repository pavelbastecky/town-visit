package controllers

import javax.inject.Inject
import play.api.mvc.{AbstractController, ControllerComponents}
import services.TravelsImportService

import scala.concurrent.ExecutionContext

class TravelsFileImportController @Inject()(cc: ControllerComponents,
                                            travelsImportService: TravelsImportService,
                                           )(implicit ec: ExecutionContext)
  extends AbstractController(cc)
  with utils.Logger
{

  def importTravels() = {
    Action(travelsImportService.importTravelsTsvStream) { implicit request =>
      Ok.chunked(request.body.map(b => s"$b\n"))
    }
  }
}
