package forex.services.oneframe.interpreters

import java.time.OffsetDateTime

import cats.effect.ConcurrentEffect
import cats.implicits._
import forex.config.OneFrameConfig
import forex.domain.{Currency, Price, Rate, Timestamp}
import forex.services.oneframe.Algebra
import forex.services.oneframe.Errors.Error
import forex.services.oneframe.interpreters.OneFrameClient.OneFrameResponse
import io.circe.generic.auto._
import org.http4s.circe._
import org.http4s.client.blaze.BlazeClientBuilder
import org.http4s.{EntityDecoder, Header, Headers, Method, Request, Uri}

import scala.concurrent.ExecutionContext

class OneFrameClient[F[_]: ConcurrentEffect](config: OneFrameConfig, execContext: ExecutionContext) extends Algebra[F] {
  implicit val responseDecoder: EntityDecoder[F, List[OneFrameResponse]] = jsonOf[F, List[OneFrameResponse]]

  override def allRates: F[Either[Error, List[forex.domain.Rate]]] = {
    val params = Currency.pairs.map(pair => s"pair=$pair").mkString("&")
    val uri    = s"${config.ratesUrl}?$params"
    val request = Request[F](
      method = Method.GET,
      uri = Uri.unsafeFromString(uri),
      headers = Headers.of(Header("token", config.defaultToken))
    )

    BlazeClientBuilder(execContext).resource.use { client =>
      {
        client
          .expect[List[OneFrameResponse]](request)
          .map(l => l.map(_.asRate))
          .attemptT
          .leftMap[Error](e => Error.OneFrameClientQueryFailed(e.getMessage))
          .value
      }
    }
  }
}

object OneFrameClient {

  case class OneFrameResponse(from: String, to: String, bid: Double, ask: Double, price: Double, time_stamp: String) {
    val asRate: Rate = {
      Rate(
        Rate.Pair(Currency.fromString(from), Currency.fromString(to)),
        Price(price),
        Timestamp(OffsetDateTime.parse(time_stamp))
      )
    }
  }

}
