package services

import java.time.LocalDate

import models.Travel
import org.scalatest.{MustMatchers, WordSpec}

class DistanceCalculatorTest extends WordSpec with MustMatchers {

  private val sourceCity = "source"
  private val destCity = "destination"

  def getTravel(city: String, traveled: Long) = Travel(0, LocalDate.now(), city, 0, Some(traveled))

  def getTravel(city: String) = Travel(0, LocalDate.now(), city, 0, None)

  def getTravel(city: String, date: String) = Travel(0, LocalDate.parse(date), city, 0, None)

  "DistanceCalculator" should {

    "findMinKilometers" should {

      "get distance between two cities" in {
        val data = Seq(
          getTravel(sourceCity, 0),
          getTravel(destCity, 500),
        )

        val r = DistanceCalculator.findMinKilometers(data, sourceCity, destCity)
        r mustBe Some(500)
      }

      "update calculated distance if better path is found" in {
        val data = Seq(
          getTravel(sourceCity, 0),
          getTravel(destCity, 500),
          getTravel(sourceCity, 1000),
          getTravel(destCity, 1100),
        )

        val r = DistanceCalculator.findMinKilometers(data, sourceCity, destCity)
        r mustBe Some(100)
      }

      "return zero if all values are the same" in {
        val data = Seq(
          getTravel(sourceCity, 500),
          getTravel(destCity, 500),
          getTravel(sourceCity, 500),
          getTravel(destCity, 500),
        )

        val r = DistanceCalculator.findMinKilometers(data, sourceCity, destCity)
        r mustBe Some(0)
      }

      "don't change calculated distance if there is no better path" in {
        val data = Seq(
          getTravel(sourceCity, 0),
          getTravel(destCity, 500),
          getTravel(destCity, 1100),
        )

        val r = DistanceCalculator.findMinKilometers(data, sourceCity, destCity)
        r mustBe Some(500)
      }

      "use better source" in {
        val data = Seq(
          getTravel(sourceCity, 0),
          getTravel(sourceCity, 400),
          getTravel(destCity, 500),
        )

        val r = DistanceCalculator.findMinKilometers(data, sourceCity, destCity)
        r mustBe Some(100)
      }

      "handle if source and dest city are equal" in {
        val data = Seq(
          getTravel(sourceCity, 0),
          getTravel(sourceCity, 400),
          getTravel(sourceCity, 500),
        )

        val r = DistanceCalculator.findMinKilometers(data, sourceCity, sourceCity)
        r mustBe Some(100)
      }

      "get no distance if there is no path from source to destination city" in {
        val data = Seq(
          getTravel(destCity, 0),
          getTravel(sourceCity, 500),
        )

        val r = DistanceCalculator.findMinKilometers(data, sourceCity, destCity)
        r mustBe None
      }

      "get no distance if there is no destination city" in {
        val data = Seq(
          getTravel(sourceCity, 500),
          getTravel(sourceCity, 1000),
        )

        val r = DistanceCalculator.findMinKilometers(data, sourceCity, destCity)
        r mustBe None
      }

      "get no distance if there is no source city" in {
        val data = Seq(
          getTravel(destCity, 500),
          getTravel(destCity, 1000),
        )

        val r = DistanceCalculator.findMinKilometers(data, sourceCity, destCity)
        r mustBe None
      }

      "get no distance if there is no city" in {
        val r = DistanceCalculator.findMinKilometers(Seq.empty, sourceCity, destCity)
        r mustBe None
      }

      "get no distance if source and dest are equal and there is only one entry" in {
        val data = Seq(
          getTravel(sourceCity, 0)
        )

        val r = DistanceCalculator.findMinKilometers(data, sourceCity, sourceCity)
        r mustBe None
      }
    }

    "findMinDuration" should {

      "get distance between two cities" in {
        val data = Seq(
          getTravel(sourceCity, "2019-01-01"),
          getTravel(destCity, "2019-02-01"),
        )

        val r = DistanceCalculator.findMinDuration(data, sourceCity, destCity)
        r mustBe Some(31)
      }

      "get distance between two cities for days before epoch" in {
        val data = Seq(
          getTravel(sourceCity, "1919-01-01"),
          getTravel(destCity, "1919-02-01"),
        )

        val r = DistanceCalculator.findMinDuration(data, sourceCity, destCity)
        r mustBe Some(31)
      }

      "update calculated distance if better path is found" in {
        val data = Seq(
          getTravel(sourceCity, "2019-01-01"),
          getTravel(destCity, "2019-02-01"),
          getTravel(sourceCity, "2019-03-01"),
          getTravel(destCity, "2019-03-10"),
        )

        val r = DistanceCalculator.findMinDuration(data, sourceCity, destCity)
        r mustBe Some(9)
      }

      "return zero if all values are the same" in {
        val data = Seq(
          getTravel(sourceCity, "2019-01-01"),
          getTravel(destCity, "2019-01-01"),
          getTravel(sourceCity, "2019-01-01"),
          getTravel(destCity, "2019-01-01"),
        )

        val r = DistanceCalculator.findMinDuration(data, sourceCity, destCity)
        r mustBe Some(0)
      }

      "don't change calculated distance if there is no better path" in {
        val data = Seq(
          getTravel(sourceCity, "2019-01-01"),
          getTravel(destCity, "2019-02-01"),
          getTravel(destCity, "2019-03-10"),
        )

        val r = DistanceCalculator.findMinDuration(data, sourceCity, destCity)
        r mustBe Some(31)
      }

      "use better source" in {
        val data = Seq(
          getTravel(sourceCity, "2019-01-01"),
          getTravel(sourceCity, "2019-01-10"),
          getTravel(destCity, "2019-02-01"),
        )

        val r = DistanceCalculator.findMinDuration(data, sourceCity, destCity)
        r mustBe Some(22)
      }

      "handle if source and dest city are equal" in {
        val data = Seq(
          getTravel(sourceCity, "2019-01-01"),
          getTravel(sourceCity, "2019-01-20"),
          getTravel(sourceCity, "2019-02-01"),
        )

        val r = DistanceCalculator.findMinDuration(data, sourceCity, sourceCity)
        r mustBe Some(12)
      }

      "get no distance if there is no path from source to destination city" in {
        val data = Seq(
          getTravel(destCity, "2019-01-01"),
          getTravel(sourceCity, "2019-02-01"),
        )

        val r = DistanceCalculator.findMinDuration(data, sourceCity, destCity)
        r mustBe None
      }

      "get no distance if there is no destination city" in {
        val data = Seq(
          getTravel(sourceCity, "2019-02-01"),
          getTravel(sourceCity, "2019-03-01"),
        )

        val r = DistanceCalculator.findMinDuration(data, sourceCity, destCity)
        r mustBe None
      }

      "get no distance if there is no source city" in {
        val data = Seq(
          getTravel(destCity, "2019-02-01"),
          getTravel(destCity, "2019-03-01"),
        )

        val r = DistanceCalculator.findMinDuration(data, sourceCity, destCity)
        r mustBe None
      }

      "get no distance if there is no city" in {
        val r = DistanceCalculator.findMinDuration(Seq.empty, sourceCity, destCity)
        r mustBe None
      }

      "get no distance if source and dest are equal and there is only one entry" in {
        val data = Seq(
          getTravel(sourceCity, "2019-02-01"),
        )

        val r = DistanceCalculator.findMinDuration(data, sourceCity, sourceCity)
        r mustBe None
      }
    }
  }
}
