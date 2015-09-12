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

  // TODO move out to utilities
  def average[T]( ts: Iterable[T] )( implicit num: Numeric[T] ) = {
    num.toDouble( ts.sum ) / ts.size
  }
  implicit def iterebleWithAvg[T:Numeric](data:Iterable[T]) = new {
    def avg = average(data)
  }

  def getTickers = tickers.values.toSeq
  def getTicker(symbol: String) = tickers(symbol.toUpperCase)
  def getDailyChange = tickers.values.map(_.stats.change).flatten.sum
  def getDailyPercentChange = {
    val changes = tickers.values.map(_.stats.percentChange.getOrElse("0").replace("%","").toDouble).toIterable
    changes.avg
  }

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
