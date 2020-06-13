package forex.services.rates

import cats.Applicative
import forex.modules.OneCache
import forex.services.rates.interpreters._

object Interpreters {
  def dummy[F[_]: Applicative](oneFrameClient: OneFrameClient[F]): Algebra[F] = new OneFrameDummy[F](oneFrameClient)
  def live[F[_]: Applicative](oneCache: OneCache): Algebra[F]                 = new OneFrameLive[F](oneCache)
}
