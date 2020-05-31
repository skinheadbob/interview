package forex.services.oneframe

import forex.domain.Rate

trait Algebra[F[_]] {
  def allRates: F[Either[Errors.Error, List[Rate]]]
}
