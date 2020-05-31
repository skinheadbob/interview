package forex.services

package object rates {
  type OneFrameClient[F[_]] = oneframe.Algebra[F]
}
