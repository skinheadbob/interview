package forex.services.rates

import cats.Applicative
import forex.services.rates.interpreters._

object Interpreters {
  def dummy[F[_]: Applicative](oneFrameClient: OneFrameClient[F]): Algebra[F] = new OneFrameDummy[F](oneFrameClient)
  def live[F[_]: Applicative](): Algebra[F]                                   = new OneFrameLive[F]()
}
