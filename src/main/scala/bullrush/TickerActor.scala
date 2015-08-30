package bullrush

import java.util.UUID

import akka.actor.{Actor, ActorRef, Props}
import akka.routing.Routee
import bullrush.RouterActor.UpdateClients
import bullrush.TickerActor._
import bullrush.yahoofinance.YahooFinanceClient

import scala.concurrent.duration._
import scala.util.{Failure, Success}

class TickerActor(router: ActorRef) extends Actor{

  import context._

  private var tickers: Seq[String] = Seq()
  private val subscribedTo: scala.collection.mutable.Map[String,Set[String]] = scala.collection.mutable.Map()
  private val tickerDetails: scala.collection.mutable.Map[String,TickerDetails] = scala.collection.mutable.Map()

  def receive = {
    case SubscribeToTicker(ticker,clientId) =>
      subscribeToTicker(ticker,clientId)
    case GetTicker(ticker) =>
      if(tickerDetails.contains(ticker)) {
        sender ! tickerDetails(ticker)
      } else sender ! new RuntimeException("Ticker not populated yed")

    case SetTickerDetails(deets) =>
      deets.foreach( deet => updateTicker(deet))

    case UpdateTickers =>
      updateTickerDetails()
  }

  @scala.throws[Exception](classOf[Exception])
  override def preStart(): Unit = {
    updateTickerDetails()
    super.preStart()
  }

  def subscribeToTicker(ticker: String, clientId: String): Unit ={
    tickers = tickers :+ ticker
    if(subscribedTo.contains(ticker)) {
      subscribedTo(ticker) = subscribedTo(ticker) + clientId
    }else{
      subscribedTo += ticker -> Set(clientId)
    }
  }

  def updateTicker(detail: TickerDetails): Unit = {
    if(detail.stats.ticker.isDefined){
      val ticker = detail.stats.ticker.get
      tickerDetails(ticker) = detail
      if(subscribedTo.contains(ticker)){
        router ! UpdateClients(detail,subscribedTo(ticker))
      }
    }
  }

  def updateTickerDetails(): Unit ={
    if(tickers.length > 1) {
      YahooFinanceClient.retrieveQuotes(tickers) onComplete {
        case Success(res) =>
          self ! SetTickerDetails(res.map(_.toTickerDetails))
        case Failure(err) =>
          println(err.getMessage)
      }
    }else if(tickers.length == 1){
      YahooFinanceClient.retrieveQuote(tickers.head) onComplete {
        case Success(res) =>
          self ! SetTickerDetails(Seq(res.toTickerDetails))
        case Failure(err) =>
          println(err.getMessage)
      }
    }

    context.system.scheduler.scheduleOnce(1 second) {
      self ! UpdateTickers
    }
  }

}

object TickerActor {
  case class SubscribeToTicker(ticker: String, clientId: String)
  case class GetTicker(ticker: String)
  case class SetTickerDetails(tickerDetails: Seq[TickerDetails])
  case object UpdateTickers

  def props(router: ActorRef): Props = {
    Props(new TickerActor(router))
  }
}

