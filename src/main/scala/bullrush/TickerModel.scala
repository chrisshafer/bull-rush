package bullrush

import spray.json.DefaultJsonProtocol


case class TickerDetails(symbol: String, price: Double)
object TickerModelProtocal extends DefaultJsonProtocol {
  implicit val tickerDetailsFormat = jsonFormat2(TickerDetails.apply)
}
/*
case class bullrush.yahoofinance.Estimates(EPSEstimateCurrentYear: Option[Double],
                     EPSEstimateNextYear: Option[Double],
                     EPSEstimateNextQuarter: Option[Double],
                     OneyrTargetPrice: Option[Double]
                      )

case class bullrush.yahoofinance.Ratios(earningsPerShare : Option[Double],
                  priceToSales : Option[Double],
                  peRatio : Option[Double],
                  pegRatio : Option[Double],
                  shortRatio : Option[Double],
                  priceBook :Option[Double]
                   )

case class bullrush.yahoofinance.HighLow(dayLow : Option[Double],
                   dayHigh : Option[Double],
                   yearLow : Option[Double],
                   yearHigh : Option[Double]
                    )

case class bullrush.yahoofinance.Stats(symbol : String,
                 ask : Double,
                 averageDailyVolume : Double,
                 bid : Double,
                 bookValue : Double,
                 change : Double,
                 currency : String,
                 lastTradeDate : String,
                 marketCap : String,
                 ebitda : String,
                 lastTradePrice : Double,
                 fiftyDayMovingAverage : Double,
                 twoHundredDayMovingAverage : Double,
                 name : String,
                 open : Double,
                 previousClose : Double,
                 lastTradeTime : String,
                 volume : Double,
                 stockExchange : String,
                 percentChange : String)
                 */