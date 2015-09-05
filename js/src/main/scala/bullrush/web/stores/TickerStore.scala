package bullrush.web.stores

import bullrush.model.TickerDetails
import bullrush.web.actions.{ReceiveTickerUpdate, TickerActions}
import bullrush.web.dispatchers._
import bullrush.web.fluxutil.ChangeEventEmitter

object TickerStore extends ChangeEventEmitter{

  def init(): Unit ={}
  def cleanStore() = {
    tickers.clear()
  }

  private val tickers : scala.collection.mutable.Map[String,TickerDetails] = scala.collection.mutable.Map()

  def getTickers = tickers.values.toSeq

  val dispatchId = TickerDispatcher.register({
    case ReceiveTickerUpdate(ticker) =>
      if( ticker.stats.ticker.isDefined && tickers.contains(ticker.stats.ticker.get)){
        tickers(ticker.stats.ticker.get) = ticker
      }else{
        tickers += ticker.stats.ticker.get -> ticker
      }
      emitChange()
  })

}
