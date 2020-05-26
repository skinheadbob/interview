package forex

import cats.effect._
import forex.config._
import forex.services.rates.interpreters.OneFrameLive
import fs2.Stream
import org.http4s.server.blaze.BlazeServerBuilder

object Main extends IOApp {

  override def run(args: List[String]): IO[ExitCode] = {
    //TODO conform to functional style
    //TODO log each poll
    OneFrameLive.startPolling()

    new Application[IO].stream.compile.drain.as(ExitCode.Success)
  }

}

class Application[F[_]: ConcurrentEffect: Timer] {

  def stream: Stream[F, Unit] =
    for {
      config <- Config.stream("app")
      module = new Module[F](config)
      _ <- BlazeServerBuilder[F]
            .bindHttp(config.http.port, config.http.host)
            .withHttpApp(module.httpApp)
            .serve
    } yield ()

}
