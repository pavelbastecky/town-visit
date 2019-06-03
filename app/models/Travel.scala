package models

import java.time.LocalDate

/**
  * Represents single travel entry DB row.
  *
  * Traveled total value represents pre-calculated value for distance calculation optimization. If the value is none
  * then value hasn't been calculated for this row.
  *
  * @param id             Primary key
  * @param date           Date when travel occurred.
  * @param city           City which was traveled to.
  * @param kilometers     Distance in kilometers traveled from previous city.
  * @param traveledTotal  Distance traveled from beginning (cumulative sum of all kilometers from beginning ordered
  *                       by date).
  */
case class Travel(id: Int,
                  date: LocalDate,
                  city: String,
                  kilometers: Int,
                  traveledTotal: Option[Long]
                 )

object Travel {
  def tupled = (Travel.apply _).tupled
}
