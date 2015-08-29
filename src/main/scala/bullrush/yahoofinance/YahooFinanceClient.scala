package bullrush.yahoofinance

import java.net.URLEncoder

import akka.actor.ActorSystem
import com.typesafe.config.ConfigFactory
import spray.client.pipelining._
import spray.http._
import spray.httpx.SprayJsonSupport._
import spray.json._
import DefaultJsonProtocol._
import scala.concurrent.Future

object YahooFinanceClient {

  implicit val system = ActorSystem()
  import bullrush.yahoofinance.YahooFinanceClient.system.dispatcher
  import SymbolJsonProtocol._

  val singleTickerPipe: HttpRequest => Future[YahooTickerDetails] = sendReceive ~> unmarshal[YahooTickerDetails]
  val multiTickerPipe: HttpRequest => Future[Seq[YahooTickerDetails]] = sendReceive ~> unmarshal[Seq[YahooTickerDetails]]


  val BASE_URL = "http://query.yahooapis.com/v1/public"

  val env = URLEncoder.encode("store://datatables.org/alltableswithkeys","UTF-8")
  val format = "json"

  def symbolYQL(ticker: String*) =
    URLEncoder.encode("select * from yahoo.finance.quotes where symbol in (\""+ticker.mkString(",")+"\")","UTF-8")
  def symbolsYQL(tickers: Seq[String]) =
    URLEncoder.encode("select * from yahoo.finance.quotes where symbol in (\""+tickers.mkString(",")+"\")","UTF-8")

  def symbolURL(ticker: String) = BASE_URL+"/yql?q="+symbolYQL(ticker)+"&format="+format+"&env="+env
  def symbolsURL(tickers: Seq[String]) = BASE_URL+"/yql?q="+symbolsYQL(tickers)+"&format="+format+"&env="+env

  def retrieveQuote(ticker: String):Future[YahooTickerDetails] =
    singleTickerPipe(Get(symbolURL(ticker)))

  def retrieveQuotes(tickers: Seq[String]):Future[Seq[YahooTickerDetails]] =
    multiTickerPipe(Get(symbolsURL(tickers)))



}
object ClientConfig {
  private val appConfig = ConfigFactory.load()
  val developer = appConfig.getConfig("clientInfo").getString("developer")
  val hubIp = appConfig.getConfig("clientInfo").getString("hub-ip")
}
