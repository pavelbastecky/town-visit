package services

import java.time.LocalDate
import java.time.temporal.ChronoUnit

import models.Travel

/**
  * Contains method to calculate minimal traveled distance or duration between two traveled cities.
  */
object DistanceCalculator {

  /**
    * Method to calculate minimal distance between cities with given names. Uses [[Travel.traveledTotal]] value as
    * optimization so it doesn't need to sum kilometers in for cities on path.
    *
    * IMPORTANT:
    * * Given set needs to contain list of visited town in order of visit. Each visit needs to have calculated
    *   [[Travel.traveledTotal]] value. If value is not defined then such row is ignored.
    *
    * * Traveled total values also needs to be in ascending order. If they are descending then result will be incorrect.
    *
    * @param seq  Ordered sequence of city travels
    * @param from Name of the city from which is traveled
    * @param to   Name of the city where is traveled to
    * @return     Calculated distance or None if no path exists.
    */
  def findMinKilometers(seq: Seq[Travel], from: String, to: String): Option[Long] =
    findMinDistance(seq, from, to)(_.traveledTotal)

  /**
    * Method to calculate minimal duration in days between cities with given names.
    *
    * IMPORTANT:
    * * Given set needs to contain list of visited town in order of visit.     *
    * * [[Travel.date]] values needs to be in ascending order. If they are descending then result will be incorrect.
    *
    * @param seq  Ordered sequence of city travels
    * @param from Name of the city from which is traveled
    * @param to   Name of the city where is traveled to
    * @return     Calculated distance or None if no path exists.
    */
  def findMinDuration(seq: Seq[Travel], from: String, to: String): Option[Long] =
    findMinDistance(seq, from, to)(t => Some(getEpochDays(t.date)))


  /* ******************************
   * Helpers
   */

  /**
    * Helper class to accumulate values needed for distance calculation.
    *
    * @param source   Latest value for the beginning of the path
    * @param minimum  Current value of calculated minimum
    */
  case class Distance(source: Option[Long] = None, minimum: Option[Long] = None)

  /**
    * High order method for calculating distances between traveled cities. Method iterates over the set of cities and
    * tries to localize minimal path from source to destination city.
    *
    * Set of cities should contain filtered rows with nly source or destination travel. Algorithm uses fact that we
    * pre-processed travels to get accumulated value from the beginning. So to get distance between two points we may
    * just subtract the values. Values also need to be sorted in ascending order so we just need to iterate the array
    * once.
    *
    * Given function is used to access field with compared value. Any value needs to be transformed to Long.
    */
  private def findMinDistance(seq: Seq[Travel], from: String, to: String)
                             (getValue: Travel => Option[Long]): Option[Long] = {
    val distance = seq.foldLeft(Distance()) { (d, travel) =>
      // If we are in destination city then try to get new minimum
      val newMinimum =
        if (travel.city == to) calculateMin(d.source, getValue(travel), d.minimum)
        else None

      // If we are in source city then update source value
      // This needs to be after previous block so it will work even for case that from is the same as to. Otherwise we
      // would change source value too early and we would compared travel to itself.
      val newSource =
        if (travel.city == from) getValue(travel)
        else None

      // Get accumulator value and ensure that we have new or old values
      Distance(
        newSource orElse d.source,
        newMinimum orElse d.minimum
      )
    }

    distance.minimum
  }

  /**
    * Safely calculates distance between source and destination and returns it if it is better than current minimum.
    * If source or destination is not defined then it keeps current minimum.
    */
  private def calculateMin(source: Option[Long],
                           destination: Option[Long],
                           currentMinimum: Option[Long]
                          ): Option[Long] = {
    for {
      src <- source
      tot <- destination

      distance = tot - src
    } yield currentMinimum.fold(distance) { min =>
      if (min < distance) min
      else distance
    }
  }

  private val epoch = LocalDate.ofEpochDay(0)

  /**
    * Used for normalization of dates - we are comparing number of days since epoch. This is safe to use even for dates
    * before epoch. Calculator works on negative values correctly.
    */
  private def getEpochDays(date: LocalDate) = {
    ChronoUnit.DAYS.between(epoch, date)
  }
}
