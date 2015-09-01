package bullrush.server.yahoofinance

import spray.httpx.SprayJsonSupport._
import spray.json._
import DefaultJsonProtocol._

object YahooJsonProtocol extends DefaultJsonProtocol {

  implicit val estimateFormat = jsonFormat4(Estimates.apply)
  implicit val ratiosFormat = jsonFormat6(Ratios.apply)
  implicit val highLowFormat = jsonFormat4(HighLow.apply)
  implicit val statsLowFormat = jsonFormat20(Stats.apply)

  implicit object YahooTickerDetailsCollectionFormat extends RootJsonFormat[Seq[YahooTickerDetails]] {
    def write(c: Seq[YahooTickerDetails]) = JsObject(
      "symbol" -> JsString("dont use this")
    )

    def read(value: JsValue) = {
      // this is an unholy mess
      value.asJsObject.fields.get("query").get.asJsObject
        .fields.get("results").get.asJsObject
        .fields.get("quote").get.asInstanceOf[JsArray].elements.map {
        x => deserializeToYahooTickerDetails(x.asJsObject)
      }
    }
  }

  implicit object TickerDetailFormat extends RootJsonFormat[YahooTickerDetails] {
    def write(c: YahooTickerDetails) = JsObject(
      "symbol" -> JsString("dont use this")
    )

    def read(value: JsValue) = {
      // this is an unholy mess
      val quote = value.asJsObject.fields.get("query").get.asJsObject
        .fields.get("results").get.asJsObject.fields.get("quote").get.asJsObject
      deserializeToYahooTickerDetails(quote)
    }

  }

  def deserializeToYahooTickerDetails(ticker: JsObject): YahooTickerDetails ={
    YahooTickerDetails(deserializeToEstimates(ticker),
      deserializeToRatios(ticker),
      deserializeHighLow(ticker),
      deserializeStats(ticker))
  }
  def deserializeToRatios(tickerJson: JsObject): Ratios ={
    val fields = Seq("EarningsShare",
      "PriceSales",
      "PERatio",
      "PEGRatio",
      "ShortRatio",
      "PriceBook")

    val ratios = tickerJson.fields.filter(name => fields.contains(name._1))
    JsObject(ratios).convertTo[Ratios]
  }
  def deserializeToEstimates(tickerJson: JsObject): Estimates ={
    val fields = Seq("EPSEstimateCurrentYear",
      "EPSEstimateNextYear",
      "EPSEstimateNextQuarter",
      "OneyrTargetPrice")
    val estimates = tickerJson.fields.filter(name => fields.contains(name._1))
    JsObject(estimates).convertTo[Estimates]

  }
  def deserializeHighLow(tickerJson: JsObject): HighLow ={
    val fields = Seq("DaysLow",
      "DaysHigh",
      "YearLow",
      "YearHigh")
    val highLow = tickerJson.fields.filter(name => fields.contains(name._1))
    JsObject(highLow).convertTo[HighLow]

  }
  def deserializeStats(tickerJson: JsObject): Stats ={
    val fields = Seq("symbol",
      "Ask",
      "AverageDailyVolume",
      "Bid",
      "BookValue",
      "Change",
      "Currency",
      "LastTradeDate",
      "MarketCapitalization",
      "EBITDA",
      "LastTradePriceOnly",
      "FiftydayMovingAverage",
      "TwoHundreddayMovingAverage",
      "Name",
      "Open",
      "PreviousClose",
      "LastTradeTime",
      "Volume",
      "StockExchange",
      "PercentChange")
    val stats = tickerJson.fields.filter(name => fields.contains(name._1))
    JsObject(stats).convertTo[Stats]

  }
}

