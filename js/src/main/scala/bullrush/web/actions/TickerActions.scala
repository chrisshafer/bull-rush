package bullrush.web.actions


import bullrush.model.TickerMessage
import bullrush.web.dispatchers.TickerDispatcher
import bullrush.web.util.TickerSocket

object TickerActions {

  private val tickerUrl = "ws://localhost:9001/events" // TODO move to config
  private val eventSocket = new TickerSocket(tickerUrl,receive)


  def subscribeToTicker(ticker: String): Unit ={
    eventSocket.send(TickerMessage(ticker,201,None,None))
  }

  private def receive(message: TickerMessage): Unit ={
    if(message.tickerDetail.isDefined){
      TickerDispatcher.dispatch(ReceiveTickerUpdate(message.tickerDetail.get))
    }
  }



}
