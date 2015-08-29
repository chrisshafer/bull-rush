import org.scalatest.concurrent.Eventually
import org.scalatest.{ShouldMatchers, FunSpec}
import spray.http.StatusCodes
import scala.concurrent.duration._
import scala.concurrent.Await
import spray.httpx.SprayJsonSupport._
import spray.json._
import DefaultJsonProtocol._


class YahooFinanceClientSpec extends FunSpec with ShouldMatchers with Eventually{
  import scala.concurrent.ExecutionContext.Implicits.global
  import SymbolJsonProtocol._

    val stockTicker = "SPWR"
    val stockTickers = Seq("SPWR","SUNE")
    describe("The yahoo finance client symbols"){
      it("Should generate a correct url"){
        assert(YahooFinanceClient.symbolURL(stockTicker) ==
          "http://query.yahooapis.com/v1/public/yql?q=select+*+from+" +
          "yahoo.finance.quotes+where+symbol+in+%28%22SPWR%22%29&" +
          "format=json&env=store%3A%2F%2Fdatatables.org%2Falltableswithkeys")
      }

      it("Should receive a quote for a symbol"){
        val response = Await.result(YahooFinanceClient.retrieveQuote(stockTicker), 10 seconds)
        assert(response.symbol == stockTicker)

      }

      it("Should receive a quote for multiple symbols"){
        val response = Await.result(YahooFinanceClient.retrieveQuotes(stockTickers), 10 seconds)
        assert(response.count{tick => stockTickers.contains(tick.symbol)} == stockTickers.size)
      }

    }
}
