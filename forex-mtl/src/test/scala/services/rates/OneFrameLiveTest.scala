package services.rates

import org.mockito.MockitoSugar
import org.scalatest.funspec.AnyFunSpec

class OneFrameLiveTest extends AnyFunSpec with MockitoSugar {

  describe("test runnable") {
    it("should run") {
      // not really a test case, just to check the proxy is runnable
      // after forex.Main is running, send a 'GET rate' request
      print("waht")

      import sttp.client.{HttpURLConnectionBackend, Identity, NothingT, SttpBackend, basicRequest, _}
      val request = basicRequest
        .get(uri"http://localhost:8081/rates?from=USD&to=JPY")
        .response(asString)
      implicit val backend: SttpBackend[Identity, Nothing, NothingT] = HttpURLConnectionBackend()

      val response = request.send().body.right.get
      println(response)
    }
  }

}
