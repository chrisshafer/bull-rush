import bullrush.server.yahoofinance.YahooFinanceClient
import org.scalatest.concurrent.Eventually
import org.scalatest.{FunSpec, ShouldMatchers}

import scala.concurrent.Await
import scala.concurrent.duration._


class YahooFinanceClientSpec extends FunSpec with ShouldMatchers with Eventually{

    val stockTicker = "SPWR"
    val stockTickers = Set("SPWR","SUNE")
    describe("The yahoo finance client symbols"){
      it("Should generate a correct url"){
        assert(YahooFinanceClient.symbolURL(stockTicker) ==
          "http://query.yahooapis.com/v1/public/yql?q=select+*+from+" +
          "yahoo.finance.quotes+where+symbol+in+%28%22SPWR%22%29&" +
          "format=json&env=store%3A%2F%2Fdatatables.org%2Falltableswithkeys")
      }

      it("Should receive a quote for a symbol"){
        val response = Await.result(YahooFinanceClient.retrieveQuote(stockTicker), 10 seconds)
        assert(response.stats.symbol.get == stockTicker)
      }

      it("Should receive a quote for multiple symbols"){
        val response = Await.result(YahooFinanceClient.retrieveQuotes(stockTickers), 10 seconds)
        assert(response.count{tick =>
          stockTickers.contains(tick.stats.symbol.get)} == stockTickers.size)
      }

    }

}
