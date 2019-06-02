package controllers

import javax.inject.Inject
import models.Version
import play.api.libs.json.Json
import play.api.mvc.{AbstractController, ControllerComponents, EssentialAction}

import scala.concurrent.ExecutionContext

class IndexController @Inject()(cc: ControllerComponents)
                               (implicit ec: ExecutionContext)
  extends AbstractController(cc)
{
  def getVersion(): EssentialAction = Action { implicit r =>
    Ok(Json.toJson(Version.getInfo))
  }
}
