import TickerActor._
import akka.actor.{Props, ActorRef, Actor}
import akka.routing.{ActorRefRoutee, RemoveRoutee, AddRoutee, Routee}
import akka.stream.actor.ActorPublisher

import scala.annotation.tailrec
import SocketEvent._
import spray.json.DefaultJsonProtocol._
import spray.json._
import scala.concurrent.duration._

import scala.util.{Failure, Success}

class TickerActor(router: ActorRef) extends Actor{

  import context._

  private var tickers: Seq[String] = Seq()
  private val tickerDetails: scala.collection.mutable.Map[String,TickerDetails] = scala.collection.mutable.Map()

  def receive = {
    case AddTicker(ticker) =>
      tickers = tickers :+ ticker

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

  def updateTicker(detail: TickerDetails): Unit = {
    tickerDetails(detail.symbol) = detail
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

    println(tickers.length)
    router ! tickerDetails.values.toList

    context.system.scheduler.scheduleOnce(1 second) {
      self ! UpdateTickers
    }
  }

}

object TickerActor {
  case class AddTicker(ticker: String)
  case class GetTicker(ticker: String)
  case class SetTickerDetails(tickerDetails: Seq[TickerDetails])
  case object UpdateTickers

  def props(router: ActorRef): Props = {
    Props(new TickerActor(router))
  }
}

