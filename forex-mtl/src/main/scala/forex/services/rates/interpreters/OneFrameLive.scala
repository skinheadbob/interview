package forex.services.rates.interpreters

import cats.Applicative
import cats.syntax.applicative._
import cats.syntax.either._
import forex.domain.Rate
import forex.modules.OneCache
import forex.services.rates.errors.Error
import forex.services.rates.{Algebra, errors}

class OneFrameLive[F[_]: Applicative] (oneCache:OneCache) extends Algebra[F] {
  override def get(pair: Rate.Pair): F[Either[errors.Error, Rate]] =
    //TODO how to catch Error and propagate msg?
    oneCache.get(pair).asRight[Error].pure[F]
}
