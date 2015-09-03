package bullrush.model


case class TickerMessage(message: String, code: Int, tickerDetail: Option[TickerDetails], messageDetails: Option[MessageDetails] )

case class MessageDetails(message: String, code: Int)