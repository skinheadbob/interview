package forex.services.rates.interpreters

import cats.Applicative
import cats.data.EitherT
import forex.domain.Rate
import forex.services.rates.errors._
import forex.services.rates.{Algebra, OneFrameClient, errors}

class OneFrameDummy[F[_]: Applicative](oneFrameClient: OneFrameClient[F]) extends Algebra[F] {

  override def get(pair: Rate.Pair): F[Error Either Rate] =
    EitherT(oneFrameClient.allRates)
      .leftMap[errors.Error](e => errors.Error.OneFrameLookupFailed(e.getMsg))
      .map(rates => rates.filter(rate => rate.pair == pair).head)
      .value

}
