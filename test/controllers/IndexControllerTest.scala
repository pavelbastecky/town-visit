package controllers

import models.{ErrorResponse, Version}
import org.scalatestplus.play._
import org.scalatestplus.play.guice._
import play.api.http.Status
import play.api.libs.json.Json
import play.api.mvc.Result
import play.api.test.CSRFTokenHelper._
import play.api.test.Helpers._
import play.api.test._

import scala.concurrent.Future

class IndexControllerTest
  extends PlaySpec
    with GuiceOneAppPerTest
    with FutureAwaits
    with DefaultAwaitTimeout
{
  "IndexController" should {
    "get version JSON" in {
      val request = FakeRequest(GET, "/api/version").withHeaders(HOST -> "localhost:9000").withCSRFToken
      val response: Future[Result] = route(app, request).get

      status(response) mustBe Status.OK

      val obj = Json.fromJson[Version](contentAsJson(response)).asOpt
      obj mustBe defined
    }

    "should return JSON error on 404 response" in {
      val request = FakeRequest(GET, "/api-not-found").withHeaders(HOST -> "localhost:9000").withCSRFToken
      val response: Future[Result] = route(app, request).get

      status(response) mustBe Status.NOT_FOUND

      val obj = Json.fromJson[ErrorResponse](contentAsJson(response)).asOpt
      obj mustBe defined
    }
  }
}
