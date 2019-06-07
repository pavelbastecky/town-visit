package repositories

import java.time.LocalDate

import javax.inject.Inject
import models.Travel
import play.api.db.slick.DatabaseConfigProvider

import scala.concurrent.{ExecutionContext, Future}

/**
  * Handles DB queries to travels table.
  *
  * This class should the smallest amount of logic ass possible. Only use for query composition and related DB
  * calculations. Other stuff should happen on service level.
  *
  * Note: If tables begin to grow then it is possible to use code generator.
  * See: http://slick.lightbend.com/doc/3.3.0/code-generation.html
  */
class TravelsRepository @Inject() (dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) {
  private val dbConfig = dbConfigProvider.get[PgProfile]

  import dbConfig._
  import profile.api._

  private class TravelsTable(tag: Tag) extends Table[Travel](tag, Some("public"), "travels") {
    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
    def date = column[LocalDate]("date")
    def city = column[String]("city")
    def kilometers = column[Int]("kilometers")
    def traveledTotal = column[Option[Long]]("traveled_total")

    def * = (id, date, city, kilometers, traveledTotal) <> ((Travel.apply _).tupled, Travel.unapply)
  }
  private val travelsTable = TableQuery[TravelsTable]

  def findById(id: Int): Future[Option[Travel]] = {
    val q = travelsTable.filter(_.id === id).result.headOption

    db.run(q)
  }

  def insert(travel: Travel) = {
    val q = travelsTable += travel

    db.run(q)
  }

  def insertAll(travels: Seq[Travel]) = {
    val q = travelsTable ++= travels

    db.run(q)
  }

  def truncate() = {
    val q = sql"""TRUNCATE TABLE "public"."travels" RESTART IDENTITY""".asUpdate

    db.run(q)
  }

  def getTravelsSorted(from: String, to: String): Future[Seq[Travel]] = {
    val q = travelsTable
      .filter(t => t.city.toLowerCase === from || t.city.toLowerCase === to)
      .sortBy(t => (t.date, t.id))
      .result

    db.run(q)
  }

  def getDailyTravels(): Future[Seq[(LocalDate, Option[Int])]] = {
    val q =travelsTable
      .groupBy(_.date)
      .map { case (date, group) =>
        (date, group.map(_.kilometers).sum)
      }
      .sortBy(_._1)
      .result

    db.run(q)
  }
}
