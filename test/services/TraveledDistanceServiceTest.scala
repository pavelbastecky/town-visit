package services

import java.time.LocalDate

import exceptions.InvalidFromToException
import models.DistanceResponses.{DistanceResponse, DurationResponse}
import models.Travel
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerTest
import play.api.test.{DefaultAwaitTimeout, FutureAwaits}
import repositories.TravelsRepository

class TraveledDistanceServiceTest
  extends PlaySpec
    with GuiceOneAppPerTest
    with FutureAwaits
    with DefaultAwaitTimeout
{
  def getTravel(city: String, date: String, kilometers: Int, total: Long) =
    Travel(0, LocalDate.parse(date), city, kilometers, Some(total))

  private val cities = Seq(
    getTravel("Praha", "2019-05-01", 0, 0),
    getTravel("Beroun", "2019-05-05", 32, 32),
    getTravel("Praha", "2019-05-06", 32, 64),
    getTravel("Beroun", "2019-05-06", 32, 96),
    getTravel("Praha", "2019-05-06", 32, 128),
    getTravel("Ostrava", "2019-05-15", 370, 498),
    getTravel("Brno", "2019-05-15", 170, 668),
    getTravel("Praha", "2019-05-16", 200, 868),
    getTravel("Olomouc", "2019-05-17", 280, 1148),
    getTravel("Brno", "2019-05-18", 80, 1228),
  )

  "TraveledDistanceServiceTest" should {
    "find distances in test set" in {
      val repo = app.injector.instanceOf[TravelsRepository]
      await(repo.truncate())
      await(repo.insertAll(cities))

      val service = app.injector.instanceOf[TraveledDistanceService]

      await(service.findMinKilometers("praha", "brno")) mustBe DistanceResponse(true, Some(280 + 80))
      await(service.findMinKilometers("brno", "praha")) mustBe DistanceResponse(true, Some(200))
      await(service.findMinKilometers("praha", "praha")) mustBe DistanceResponse(true, Some(64))

      await(service.findMinKilometers("brno", "beroun")) mustBe DistanceResponse(false, None)
      await(service.findMinKilometers("praha", "kolin")) mustBe DistanceResponse(false, None)
      await(service.findMinKilometers("kolin", "praha")) mustBe DistanceResponse(false, None)
      await(service.findMinKilometers("kolin", "nymburk")) mustBe DistanceResponse(false, None)

      await(service.findMinDuration("praha", "brno")) mustBe DurationResponse(true, Some(2))
      await(service.findMinDuration("brno", "praha")) mustBe DurationResponse(true, Some(1))
      await(service.findMinDuration("praha", "praha")) mustBe DurationResponse(true, Some(0))

      await(service.findMinDuration("kolin", "nymburk")) mustBe DurationResponse(false, None)

      an[InvalidFromToException] must be thrownBy
        await(service.findMinDuration(" ", ""))
    }
  }
}
