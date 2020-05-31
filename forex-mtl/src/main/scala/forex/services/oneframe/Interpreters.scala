package forex.services.oneframe

import cats.effect.ConcurrentEffect
import forex.config.OneFrameConfig
import forex.services.oneframe.interpreters.OneFrameClient

import scala.concurrent.ExecutionContext

object Interpreters {
  def oneFrameHttpClient[F[_]: ConcurrentEffect](config: OneFrameConfig,
                                                 execContext: ExecutionContext): OneFrameClient[F] =
    new OneFrameClient[F](config, execContext)
}
