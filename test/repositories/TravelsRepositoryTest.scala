package repositories

import java.time.LocalDate

import models.Travel
import org.scalatest.BeforeAndAfter
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerTest
import play.api.test.{DefaultAwaitTimeout, FutureAwaits}

class TravelsRepositoryTest
  extends PlaySpec
    with GuiceOneAppPerTest
    with FutureAwaits
    with DefaultAwaitTimeout
    with BeforeAndAfter
{
  lazy val now = LocalDate.now
  lazy val city = "Praha"
  lazy val traveled = 50

  "TravelsRepositoryTest" should {
    "verify repository setup" in {
      // This test just verifies that slick can connect in memory H2 and that tables was successfully created

      val repo = app.injector.instanceOf[TravelsRepository]

      await(repo.insert(Travel(0, now, city, traveled, None)))

      val r = await(repo.findById(1))
      r mustBe Some(Travel(1, now, city, traveled, None))
    }
  }
}
