package forex.modules

import java.time.OffsetDateTime
import java.util.concurrent.TimeUnit

import com.google.common.cache.CacheBuilder
import forex.config.OneCacheConfig
import forex.domain.{Currency, Price, Rate, Timestamp}
import forex.modules.OneCache.OneFrameRate
import monix.execution.Scheduler.{global => scheduler}
import org.json4s.native.Serialization
import scalacache.Entry
import scalacache.guava.GuavaCache
import sttp.client._
import sttp.client.json4s._

// self-refreshing cache
class OneCache(config: OneCacheConfig) {

  def get(pair: Rate.Pair): Rate = {
    import scalacache.modes.sync._
    cache.get(toCacheKey(pair)).get
  }

  private implicit val serialization: Serialization.type = org.json4s.native.Serialization

  //TODO does 'Key' have to be String?
  private val cache: GuavaCache[Rate] = GuavaCache(CacheBuilder.newBuilder().build[String, Entry[Rate]])

  private def toCacheKey(rate: Rate): String =
    toCacheKey(rate.pair)

  private def toCacheKey(pair: Rate.Pair): String = {
    import cats.syntax.show._
    s"${pair.from.show}${pair.to.show}"
  }

  private val uri = s"${config.ratesUrl}?${Currency.pairs.map(pair => s"pair=$pair").mkString("&")}"

  private val request = basicRequest
    .header("token", config.defaultToken)
    .get(uri"$uri")
    .response(asJson[List[OneFrameRate]])
  private implicit val backend: SttpBackend[Identity, Nothing, NothingT] = HttpURLConnectionBackend()

  private def populateCache(): Unit = {
    val rates = request.send().body.right.get.map(_.asRate)
    for (rate <- rates) {
      import scalacache.modes.sync._
      cache.put(toCacheKey(rate))(rate, ttl = None)
    }
  }
  //TODO add a 'manual refresh' trigger in case 'auto refresh' failed
  scheduler.scheduleAtFixedRate(0, config.refreshIntervalSec, TimeUnit.SECONDS, () => this.populateCache())
}

object OneCache {

  case class OneFrameRate(from: String, to: String, bid: Double, ask: Double, price: Double, time_stamp: String) {
    val asRate: Rate = {
      Rate(
        Rate.Pair(Currency.fromString(from), Currency.fromString(to)),
        Price(price),
        Timestamp(OffsetDateTime.parse(time_stamp))
      )
    }
  }

}
