package bullrush.yahoofinance

import bullrush.TickerDetails
import spray.json._
import spray.httpx.SprayJsonSupport._
import spray.json._
import DefaultJsonProtocol._

case class YahooTickerDetails(estimates: Estimates, ratios: Ratios, highLow: HighLow, stats: Stats){
  def toTickerDetails: TickerDetails ={
    TickerDetails(estimates,ratios,highLow,stats)
  }

  private implicit def toOptDubs(dub: Option[String]): Option[Double] = dub map { _.toDouble }
  private implicit def statsToStats(stats: Stats): bullrush.Stats ={
    bullrush.Stats(
      stats.symbol,stats.Ask,stats.AverageDailyVolume,
      stats.Bid,stats.BookValue,stats.Change,
      stats.Currency,stats.LastTradeDate,stats.MarketCapitalization,
      stats.EBITDA,stats.LastTradePriceOnly,stats.FiftydayMovingAverage,
      stats.TwoHundreddayMovingAverage,stats.Name,stats.Open,
      stats.PreviousClose,stats.LastTradeTime,stats.Volume,
      stats.StockExchange,stats.PercentChange
    )
  }
  private implicit def highLowToHighLow(highLow: HighLow): bullrush.HighLow ={
    bullrush.HighLow(
      highLow.DaysLow,highLow.DaysHigh,
      highLow.YearLow,highLow.YearHigh
    )
  }
  private implicit def ratiosToRatios(ratios: Ratios): bullrush.Ratios ={
    bullrush.Ratios(
      ratios.EarningsShare,
      ratios.PriceSales,
      ratios.PERatio,
      ratios.PEGRatio,
      ratios.ShortRatio,
      ratios.PriceBook
    )
  }
  private implicit def estimatesToEstimates(estimates: Estimates): bullrush.Estimates ={
    bullrush.Estimates(
      estimates.EPSEstimateCurrentYear,estimates.EPSEstimateNextYear,
      estimates.EPSEstimateNextQuarter,estimates.OneyrTargetPrice
    )
  }
}

case class Stats(symbol : Option[String],
                 Ask : Option[String],
                 AverageDailyVolume : Option[String],
                 Bid : Option[String],
                 BookValue : Option[String],
                 Change : Option[String],
                 Currency : Option[String],
                 LastTradeDate : Option[String],
                 MarketCapitalization : Option[String],
                 EBITDA : Option[String],
                 LastTradePriceOnly : Option[String],
                 FiftydayMovingAverage : Option[String],
                 TwoHundreddayMovingAverage : Option[String],
                 Name : Option[String],
                 Open : Option[String],
                 PreviousClose : Option[String],
                 LastTradeTime : Option[String],
                 Volume : Option[String],
                 StockExchange : Option[String],
                 PercentChange : Option[String]
                  )

case class Ratios(EarningsShare : Option[String],
                  PriceSales : Option[String],
                  PERatio : Option[String],
                  PEGRatio : Option[String],
                  ShortRatio : Option[String],
                  PriceBook :Option[String]
                   )

case class HighLow(DaysLow : Option[String],
                   DaysHigh : Option[String],
                   YearLow : Option[String],
                   YearHigh : Option[String]
                    )

case class Estimates(EPSEstimateCurrentYear: Option[String],
                     EPSEstimateNextYear: Option[String],
                     EPSEstimateNextQuarter: Option[String],
                     OneyrTargetPrice: Option[String]
                      )