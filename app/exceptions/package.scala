import play.api.http.Status
import play.api.libs.json.{JsObject, Json}

package object exceptions {

  /**
    * Base class for exceptions thrown by by API endpoints. When this type is thrown from controller then error handler
    * will print nice formatted massage to the user.
    *
    * @param message  Human readable message explaining the problem.
    * @param status   Http response code to return from API/
    * @param payload  Another data relevant for the exceptional state (IDs of involved objects, etc)
    * @param cause    Encapsulated exception.
    */
  abstract class ApiException(val message: String,
                              val status: Int = Status.BAD_REQUEST,
                              val payload: Option[JsObject] = None,
                              val cause: Option[Throwable] = None,
                             ) extends RuntimeException(message, cause.orNull)

  /**
    * General SQL exception. Providing error message for the api endpoint without getting implementation details.
    */
  case class SqlException(error: Throwable) extends ApiException(
    "Error querying the database",
    status = Status.INTERNAL_SERVER_ERROR,
    cause = Some(error)
  )

  /**
    * Bad request for duration calculation
    */
  case class InvalidFromToException(from: String, to: String) extends ApiException(
    "Both FROM and TO parameters must be non empty",
    payload = Some(Json.obj("from" -> from, "to" -> to))
  )
}
