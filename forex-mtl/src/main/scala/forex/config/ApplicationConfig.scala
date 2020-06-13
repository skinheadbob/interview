package forex.config

import scala.concurrent.duration.FiniteDuration

case class ApplicationConfig(
                              http: HttpConfig,
                              oneFrame: OneFrameConfig,
                              oneCache: OneCacheConfig
                            )

case class HttpConfig(
                       host: String,
                       port: Int,
                       timeout: FiniteDuration
                     )

case class OneFrameConfig(
                           ratesUrl: String,
                           defaultToken: String
                         )

case class OneCacheConfig(
                           ratesUrl: String,
                           defaultToken:String,
                           refreshIntervalSec:Int
                         )
