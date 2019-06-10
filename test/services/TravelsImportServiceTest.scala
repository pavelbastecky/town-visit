package services

import java.time.LocalDate

import akka.util.ByteString
import models.Travel
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.test.{DefaultAwaitTimeout, FutureAwaits}
import repositories.TravelsRepository
import services.TravelsImportService.ProcessedBatch

class TravelsImportServiceTest
  extends PlaySpec
    with GuiceOneAppPerSuite
    with FutureAwaits
    with DefaultAwaitTimeout
{
  def getTravel(city: String, date: String, kilometers: Int) =
    Travel(0, LocalDate.parse(date), city, kilometers, None)

  private val cities = Seq(
    getTravel("Praha", "2019-05-01", 0),
    getTravel("Beroun", "2019-05-05", 32),
    getTravel("Praha", "2019-05-06", 32),
    getTravel("Beroun", "2019-05-06", 32),
    getTravel("Praha", "2019-05-06", 32),
    getTravel("Ostrava", "2019-05-15", 370),
    getTravel("Brno", "2019-05-15", 170),
    getTravel("Praha", "2019-05-16", 200),
    getTravel("Olomouc", "2019-05-17", 280),
    getTravel("Brno", "2019-05-18", 80),
  )

  "TravelsImportServiceTest" should {
    "parseTravel" should {
      "parse proper formatted line" in {
        val service = app.injector.instanceOf[TravelsImportService]
        val r = service.parseTravel(ByteString("2019-01-01\tPraha\t100"))

        r mustBe Some(Travel(0, LocalDate.parse("2019-01-01"), "Praha", 100, None))
      }
      "ignore line with invalid number of columns" in {
        val service = app.injector.instanceOf[TravelsImportService]
        val r = service.parseTravel(ByteString("2019-01-01\tPraha\t100\tinvalid"))

        r mustBe None
      }
      "ignore line with invalid date" in {
        val service = app.injector.instanceOf[TravelsImportService]
        val r = service.parseTravel(ByteString("AAA\tPraha\t100"))

        r mustBe None
      }
      "ignore line with invalid number of kilometers" in {
        val service = app.injector.instanceOf[TravelsImportService]
        val r = service.parseTravel(ByteString("2019-01-01\tPraha\tAAA"))

        r mustBe None
      }
      "ignore empty line" in {
        val service = app.injector.instanceOf[TravelsImportService]
        val r = service.parseTravel(ByteString(""))

        r mustBe None
      }
      "ignore header line" in {
        val service = app.injector.instanceOf[TravelsImportService]
        val r = service.parseTravel(ByteString("Date\tCity\tKilometers"))

        r mustBe None
      }
    }

    "processBatch" should {
      "insert all travels to the database" in {
        val repo = app.injector.instanceOf[TravelsRepository]
        await(repo.truncate())

        val service = app.injector.instanceOf[TravelsImportService]

        val r = await(service.processBatch(cities))
        r mustBe Some(ProcessedBatch(cities.size, cities.head.date))

        await(repo.count()) mustBe cities.size
      }

      "do nothing for empty batch" in {
        val repo = app.injector.instanceOf[TravelsRepository]
        await(repo.truncate())

        val service = app.injector.instanceOf[TravelsImportService]

        val r = await(service.processBatch(Seq()))
         r mustBe None

        await(repo.count()) mustBe 0
      }
    }

    "cumulativeResults" should {
      "get left date" in {
        val left = ProcessedBatch(50, LocalDate.parse("2019-01-01"))
        val right = ProcessedBatch(100, LocalDate.parse("2019-05-01"))

        val service = app.injector.instanceOf[TravelsImportService]
        val r = service.cumulativeResults(left, right)
        r mustBe ProcessedBatch(150, left.minDate)
      }

      "get right date" in {

        val left = ProcessedBatch(50, LocalDate.parse("2019-01-01"))
        val right = ProcessedBatch(100, LocalDate.parse("2018-05-01"))

        val service = app.injector.instanceOf[TravelsImportService]
        val r = service.cumulativeResults(left, right)
        r mustBe ProcessedBatch(150, right.minDate)
      }
    }

    // FIXME: recalculateTotalKilometers
    //  this DB queries in this method can't be tested using H2 due to custom postgres operators
  }
}
