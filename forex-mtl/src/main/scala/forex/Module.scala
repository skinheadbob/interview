package forex

import cats.effect.{ConcurrentEffect, Timer}
import forex.config.ApplicationConfig
import forex.http.rates.RatesHttpRoutes
import forex.programs._
import forex.services._
import forex.services.oneframe.interpreters.OneFrameClient
import org.http4s._
import org.http4s.implicits._
import org.http4s.server.middleware.{AutoSlash, Timeout}

import scala.concurrent.ExecutionContext.global

class Module[F[_]: ConcurrentEffect: Timer](config: ApplicationConfig) {

  private val oneFrameClient: OneFrameClient[F] = oneframe.Interpreters.oneFrameHttpClient[F](config.oneFrame, global)

//  private val ratesService: RatesService[F] = RatesServices.dummy[F](oneFrameClient)
  private val ratesService: RatesService[F] = RatesServices.live[F]()

  private val ratesProgram: RatesProgram[F] = RatesProgram[F](ratesService)

  private val ratesHttpRoutes: HttpRoutes[F] = new RatesHttpRoutes[F](ratesProgram).routes

  type PartialMiddleware = HttpRoutes[F] => HttpRoutes[F]
  type TotalMiddleware   = HttpApp[F] => HttpApp[F]

  private val routesMiddleware: PartialMiddleware = {
    { http: HttpRoutes[F] =>
      AutoSlash(http)
    }
  }

  private val appMiddleware: TotalMiddleware = { http: HttpApp[F] =>
    Timeout(config.http.timeout)(http)
  }

  private val http: HttpRoutes[F] = ratesHttpRoutes

  val httpApp: HttpApp[F] = appMiddleware(routesMiddleware(http).orNotFound)

}
