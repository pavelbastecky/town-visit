import io.gatling.core.Predef._
import io.gatling.core.structure.ScenarioBuilder
import io.gatling.http.Predef._
import io.gatling.http.protocol.HttpProtocolBuilder

import scala.concurrent.duration._
import scala.language.postfixOps

// run with "sbt gatling:test" on another machine so you don't have resources contending.
// http://gatling.io/docs/2.2.2/general/simulation_structure.html#simulation-structure
class GatlingSpec extends Simulation {

  // change this to another machine, make sure you have Play running in producion mode
  // i.e. sbt stage / sbt dist and running the script
  val httpConf: HttpProtocolBuilder = http.baseUrl("http://localhost:9000/api/traveled/distance?from=praha&to=brno")

  val getDistance: ScenarioBuilder = scenario("TraveledDistance")
    .exec(distanceCall("uv", "zz")) // Random distance
    .exec(durationCall("aa", "bb")) // Random duration
    .exec(dailyChart())
    .exec(distanceCall("d4", "kb")) // Random  distance small occurrences
    .exec(distanceCall("mb", "kn")) // Missing distance
    .exec(weeklyChart())

  setUp(
    getDistance.inject(rampUsers(10000).during(100.seconds)).protocols(httpConf)
  )

  def distanceCall(from: String, to: String) = {
    val uri = s"/api/traveled/distance?from=$from&to=$to"
    http(s"Distance-$from-$to").get(uri).check(status.is(200))
  }

  def durationCall(from: String, to: String) = {
    val uri = s"/api/traveled/distance?from=$from&to=$to"
    http(s"Duration-$from-$to").get(uri).check(status.is(200))
  }

  def dailyChart() = {
    val uri = s"/api/traveled/chart/daily"
    http(s"Daily-Chart").get(uri).check(status.is(200))
  }
  def weeklyChart() = {
    val uri = s"/api/traveled/chart/daily"
    http(s"Weekly-Chart").get(uri).check(status.is(200))
  }
}
