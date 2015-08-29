import spray.json._
import spray.httpx.SprayJsonSupport._
import spray.json._
import DefaultJsonProtocol._

case class TickerDetails(symbol: String, price: Double)
case class YahooTickerDetails(estimates: Estimates, ratios: Ratios, HighLow: HighLow, stats: Stats){
  def toTickerDetails: TickerDetails ={
    TickerDetails(stats.symbol.getOrElse("Unavailable"),stats.LastTradePriceOnly.getOrElse("").toDouble)
  }
}
case class Estimates(EPSEstimateCurrentYear: Option[String],
                     EPSEstimateNextYear: Option[String],
                     EPSEstimateNextQuarter: Option[String],
                     OneyrTargetPrice: Option[String]
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


object SymbolJsonProtocol extends DefaultJsonProtocol {

  implicit val tickerDetailsFormat = jsonFormat2(TickerDetails.apply)
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
/*
    tickerJson.getFields("EarningsShare",
      "PriceSales",
      "PERatio",
      "PEGRatio",
      "ShortRatio",
      "PriceBook") match {
      case Seq(
      JsString(earningsPerShare),
      JsString(priceToSales),
      JsString(peRatio),
      JsString(pegRatio),
      JsString(shortRatio),
      JsString(priceBook)
      ) =>
        println("PERATIO"++pegRatio)
        Ratios(
        earningsPerShare.toDouble,
        priceToSales.toDouble,
        peRatio.toDouble,
        pegRatio.toDouble,
        shortRatio.toDouble,
        priceBook.toDouble
        )
      case _ => throw new DeserializationException("Complex expected")
    }

        tickerJson.getFields() match {
      case Seq(
      JsString(epsEstimateCurrentYear),
      JsString(epsEstimateNextYear),
      JsString(epsEstimateNextQuarter),
      JsString(oneYearTarget)) =>
        Estimates(
          epsEstimateCurrentYear.toDouble,
          epsEstimateNextYear.toDouble,
          epsEstimateNextQuarter.toDouble,
          oneYearTarget.toDouble
        )
      case _ => throw new DeserializationException("Complex expected")
    }
        tickerJson.getFields("DaysLow",
      "DaysHigh",
      "YearLow",
      "YearHigh") match {
      case Seq(
      JsString(dayLow),
      JsString(dayHigh),
      JsString(yearLow),
      JsString(yearHigh)) =>
        HighLow(
          dayLow.toDouble,
          dayHigh.toDouble,
          yearLow.toDouble,
          yearHigh.toDouble
        )
      case _ => throw new DeserializationException("Complex expected")
    }
        tickerJson.getFields("symbol",
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
      "PercentChange") match {

      case Seq(
      JsString(ticker),
      JsString(ask),
      JsString(averageDailyVolume),
      JsString(bid),
      JsString(bookValue),
      JsString(change),
      JsString(currency),
      JsString(lastTradeDate),
      JsString(marketCap),
      JsString(ebitda),
      JsString(lastTradePrice),
      JsString(fiftyDayMovingAverage),
      JsString(twoHundredDayMovingAverage),
      JsString(name),
      JsString(open),
      JsString(previousClose),
      JsString(lastTradeTime),
      JsString(volume),
      JsString(stockExchange),
      JsString(percentChange)) =>
        Stats(ticker,
          ask.toDouble,
          averageDailyVolume.toDouble,
          bid.toDouble,
          bookValue.toDouble,
          change.toDouble,
          currency,
          lastTradeDate,
          marketCap,
          ebitda,
          lastTradePrice.toDouble,
          fiftyDayMovingAverage.toDouble,
          twoHundredDayMovingAverage.toDouble,
          name,
          open.toDouble,
          previousClose.toDouble,
          lastTradeTime,
          volume.toDouble,
          stockExchange,
          percentChange)
      case _ => throw new DeserializationException("Complex expected")
    }

        tickerJson.getFields("symbol",
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
      "PercentChange") match {

      case Seq(
      JsString(ticker),
      JsString(ask),
      JsString(averageDailyVolume),
      JsString(bid),
      JsString(bookValue),
      JsString(change),
      JsString(currency),
      JsString(lastTradeDate),
      JsString(marketCap),
      JsString(ebitda),
      JsString(lastTradePrice),
      JsString(fiftyDayMovingAverage),
      JsString(twoHundredDayMovingAverage),
      JsString(name),
      JsString(open),
      JsString(previousClose),
      JsString(lastTradeTime),
      JsString(volume),
      JsString(stockExchange),
      JsString(percentChange)) =>
        Stats(ticker,
          ask.toDouble,
          averageDailyVolume.toDouble,
          bid.toDouble,
          bookValue.toDouble,
          change.toDouble,
          currency,
          lastTradeDate,
          marketCap,
          ebitda,
          lastTradePrice.toDouble,
          fiftyDayMovingAverage.toDouble,
          twoHundredDayMovingAverage.toDouble,
          name,
          open.toDouble,
          previousClose.toDouble,
          lastTradeTime,
          volume.toDouble,
          stockExchange,
          percentChange)
      case _ => throw new DeserializationException("Complex expected")
    }
 */
}
