package module

import exceptions.ApiException
import javax.inject.{Inject, Provider}
import models.ErrorResponse
import play.api.{Configuration, Environment, OptionalSourceMapper, UsefulException}
import play.api.http.{DefaultHttpErrorHandler, Status}
import play.api.http.Status.{BAD_REQUEST, FORBIDDEN, NOT_FOUND}
import play.api.libs.json.Json
import play.api.mvc.{RequestHeader, Result, Results}
import play.api.mvc.Results.InternalServerError
import play.api.routing.Router
import utils.Logger

import scala.concurrent.Future

class ApiErrorHandler @Inject()(env: Environment,
                                config: Configuration,
                                sourceMapper: OptionalSourceMapper,
                                router: Provider[Router]
                               )
  extends DefaultHttpErrorHandler(env, config, sourceMapper, router)
    with Logger
{
  /**
    * Intercept all client exceptions to prevent printing default HTML error page. Print JSON error response instead.
    */
  override def onClientError(request: RequestHeader,
                             statusCode: Int,
                             message: String): Future[Result] = {
    logger.debug(
      s"onClientError: statusCode = $statusCode, uri = ${request.uri}, message = $message")

    Future.successful {
      val result = statusCode match {
        case BAD_REQUEST =>
          Results.BadRequest(Json.toJson(ErrorResponse(getMessage(message, "Bad request"))))
        case FORBIDDEN =>
          Results.Forbidden(Json.toJson(ErrorResponse(getMessage(message, "Forbidden"))))
        case NOT_FOUND =>
          val msg = "Resource not found " + request.uri
          Results.NotFound(Json.toJson(ErrorResponse(getMessage(message, msg))))
        case _ if statusCode >= 400 && statusCode < 500 =>
          Results.Status(statusCode)
        case _ =>
          val msg =
            s"onClientError invoked with non client error status code $statusCode: $message"
          throw new IllegalArgumentException(msg)
      }
      result
    }
  }

  /**
    * Handles custom api exceptions and transforms them to error responses
    */
  override def onServerError(request: RequestHeader, exception: Throwable): Future[Result] = {
    exception match {
      case e: ApiException =>
        Future.successful(Results.Status(e.status)(Json.toJson(ErrorResponse(e))))

      case e: Throwable =>
        super.onServerError(request, e)
    }
  }

  /**
    * We want to know whats happening while server error on dev server - return message, useful for debugging
    */
  override protected def onDevServerError(request: RequestHeader,
                                          exception: UsefulException): Future[Result] = {
    Future.successful (
      InternalServerError(Json.toJson(ErrorResponse(exception)))
    )
  }

  /**
    * On prod just return internal error so client won't see server internals. Developer will check logs.
    */
  override protected def onProdServerError(request: RequestHeader,
                                           exception: UsefulException): Future[Result] = {
    Future.successful(InternalServerError)
  }

  private def getMessage(message: String, default: String) = {
    if (message.nonEmpty) {
      message
    } else {
      default
    }
  }
}
