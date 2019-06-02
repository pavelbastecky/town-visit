package models

import exceptions.ApiException
import play.api.libs.json.{Format, JsValue, Json}

case class ErrorResponse(status: String,
                         message: String,
                         payload: Option[JsValue]
                        )

object ErrorResponse {
  implicit val format: Format[ErrorResponse] = Json.format[ErrorResponse]

  def apply(message: String): ErrorResponse = new ErrorResponse("error", message, None)

  def apply(e: ApiException): ErrorResponse = new ErrorResponse("error", e.message, e.payload)

  def apply(e: Throwable): ErrorResponse = new ErrorResponse("error", e.getMessage, None)
}
