package forex

package object services {
  import scala.concurrent.duration._

  type RatesService[F[_]] = rates.Algebra[F]
  final val RatesServices = rates.Interpreters

  //TODO move to resources/application.conf
  final val OneFrameUrl = "http://localhost:8080/rates"
  //TODO token may need refresh
  final val OneFrameToken = "10dc303535874aeccc86a8251e6992f5"
  final val SLA_TTL = 5.minutes
  final val CacheRefreshIntervalSec = 2 * 60
}
