package forex.services.oneframe

object Errors {

  sealed trait Error {
    def getMsg:String
  }
  object Error {
    final case class OneFrameClientQueryFailed(msg: String) extends Error {
      override def getMsg: String = msg
    }
  }

}