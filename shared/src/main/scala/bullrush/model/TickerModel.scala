package bullrush.model

case class TickerDetails(estimates: Estimates, ratios: Ratios, HighLow: HighLow, stats: Stats)
case class HighLow(dayLow : Option[Double],
                   dayHigh : Option[Double],
                   yearLow : Option[Double],
                   yearHigh : Option[Double]
                    )
case class Ratios(earningsPerShare : Option[Double],
                  priceToSales : Option[Double],
                  peRatio : Option[Double],
                  pegRatio : Option[Double],
                  shortRatio : Option[Double],
                  priceBook :Option[Double]
                   )
case class Estimates(epsEstimateCurrentYear: Option[Double],
                     epsEstimateNextYear: Option[Double],
                     epsEstimateNextQuarter: Option[Double],
                     oneYearPriceTarget: Option[Double]
                      )
case class Stats(ticker : Option[String],
                 ask : Option[Double],
                 averageDailyVolume : Option[Double],
                 bid : Option[Double],
                 bookValue : Option[Double],
                 change : Option[Double],
                 currency : Option[String],
                 lastTradeDate : Option[String],
                 marketCap : Option[String],
                 ebitda : Option[String],
                 lastPrice : Option[Double],
                 fiftyDayMovingAverage : Option[Double],
                 twoHundredDayMovingAverage : Option[Double],
                 name : Option[String],
                 open : Option[Double],
                 previousClose : Option[Double],
                 lastTradeTime : Option[String],
                 volume : Option[Double],
                 stockExchange : Option[String],
                 percentChange : Option[String]
                  )

