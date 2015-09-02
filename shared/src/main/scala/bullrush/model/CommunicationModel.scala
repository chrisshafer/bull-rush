package bullrush.model

case class TickerUpdate(message: String, code: Int = 1, tickerDetail: TickerDetails)
case class SocketEvent(message: String, user: String, code: Int = 1)