package forex.services.rates.interpreters

import java.time.OffsetDateTime
import java.util.concurrent.TimeUnit

import cats.Applicative
import cats.syntax.applicative._
import cats.syntax.either._
import com.google.common.cache.CacheBuilder
import forex.domain.{ Currency, Price, Rate, Timestamp }
import forex.services.rates.errors.Error
import forex.services.rates.{ errors, Algebra }
import forex.services.{ OneFrameToken, OneFrameUrl, SLA_TTL }
import monix.execution.Cancelable
import org.json4s.native.Serialization
import scalacache.Entry
import scalacache.guava.GuavaCache

class OneFrameLive[F[_]: Applicative] extends Algebra[F] {
  override def get(pair: Rate.Pair): F[Either[errors.Error, Rate]] =
    //TODO how to catch Error and propagate msg?
    OneFrameLive.sync_get(pair).asRight[Error].pure[F]
}

object OneFrameLive {

  import monix.execution.Scheduler.{ global => scheduler }
  import sttp.client._
  import sttp.client.json4s._

  implicit val serialization: Serialization.type = org.json4s.native.Serialization

  def startPolling(): Cancelable =
    scheduler.scheduleAtFixedRate(0, 5, TimeUnit.SECONDS, () => OneFrameLive.populateCache())

  //TODO does 'Key' have to be String?
  private val cache: GuavaCache[Rate] = GuavaCache(CacheBuilder.newBuilder().build[String, Entry[Rate]])

  private def sync_get(pair: Rate.Pair) = {
    import scalacache.modes.sync._
    cache.get(toCacheKey(pair)).get
  }

  private def toCacheKey(rate: Rate): String =
    toCacheKey(rate.pair)

  private def toCacheKey(pair: Rate.Pair): String = {
    import cats.syntax.show._
    s"${pair.from.show}${pair.to.show}"
  }

  //TODO use BlazeClient to reduce lib dependency
  case class OneFrameRate(from: String, to: String, bid: Double, ask: Double, price: Double, time_stamp: String) {
    val asRate: Rate = {
      Rate(
        Rate.Pair(Currency.fromString(from), Currency.fromString(to)),
        Price(price),
        Timestamp(OffsetDateTime.parse(time_stamp))
      )
    }
  }

  private val uri = s"$OneFrameUrl?${Currency.pairs.map(pair => s"pair=$pair").mkString("&")}"
  private val request = basicRequest
    .header("token", OneFrameToken)
    .get(uri"$uri")
    .response(asJson[List[OneFrameRate]])
  implicit val backend: SttpBackend[Identity, Nothing, NothingT] = HttpURLConnectionBackend()

  private def populateCache(): Unit = {
    val rates = request.send().body.right.get.map(_.asRate)
    for (rate <- rates) {
      import scalacache.modes.sync._
      cache.put(toCacheKey(rate))(rate, ttl = Some(SLA_TTL))
    }
  }
}
