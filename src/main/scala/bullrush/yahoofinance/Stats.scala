package bullrush.yahoofinance

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
