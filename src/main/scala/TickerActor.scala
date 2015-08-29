import akka.actor.{ActorRef, Actor}
import akka.routing.{ActorRefRoutee, RemoveRoutee, AddRoutee, Routee}
import akka.stream.actor.ActorPublisher

import scala.annotation.tailrec
import SocketEvent._
import spray.json.DefaultJsonProtocol._
import spray.json._
import scala.concurrent.duration._


import scala.util.{Failure, Success}

class TickerActor extends Actor{

  import context._


  case class AddTicker(ticker: String)
  case class GetTicker(ticker: String)
  case class SetPrices(tickerDetails: Seq[TickerDetails])
  case object UpdateTickers
  private var tickers: Seq[String] = Seq()
  private val tickerDetails: scala.collection.mutable.Map[String,TickerDetails] = scala.collection.mutable.Map()

  def updateTickerDetails(): Unit ={
    YahooFinanceClient.retrieveQuotes(tickers) onComplete {
      case Success(res) =>
        self ! SetPrices(res)
        context.system.scheduler.scheduleOnce(1 hour){
          self ! UpdateTickers
        }
      case Failure(err) =>
        context.system.scheduler.scheduleOnce(1 second){
          self ! UpdateTickers
        }
    }
  }

  def receive = {
    case AddTicker(ticker) =>
      tickers = tickers :+ ticker
    case GetTicker(ticker) =>
      sender ! tickerDetails.get(ticker)
    case SetPrices(tickers) =>
      tickers.foreach( ticker => tickerDetails - ticker.symbol)
    case UpdateTickers =>
      updateTickerDetails()
  }
}

case object SendStats
class RouterActor extends Actor {

  private var clients = Set[Routee]()

  def receive = {
    case ar: AddRoutee =>
      println("Adding Routee")
      clients = clients + ar.routee
    case rr: RemoveRoutee =>
      println("Removing Routee")
      clients = clients - rr.routee
    case msg: SocketEvent =>
      clients.foreach(_.send(msg.toJson.toString() ,sender))
    case SendStats =>
      clients.foreach(_.send(SocketEvent(clients.size.toString,"SERVER",2).toJson.toString(),sender))
    case msg => clients.foreach(_.send(msg, sender))
  }
}
class RouterPublisher(router: ActorRef) extends ActorPublisher[String] {

  case object QueueUpdated

  import akka.stream.actor.ActorPublisherMessage._
  import scala.collection.mutable

  private val queue = mutable.Queue[String]()
  val MaxBufferSize = 100
  var queueUpdated = false

  override def preStart(): Unit = router ! AddRoutee(ActorRefRoutee(self))
  override def postStop(): Unit = router ! RemoveRoutee(ActorRefRoutee(self))

  def receive = {

    case message: String  =>
      if (queue.size == MaxBufferSize) {
        queue.dequeue()
        println("Oh noes ! Buffer full !")
      }
      queue += message
      if (!queueUpdated) {
        queueUpdated = true
        self ! QueueUpdated
      }
    case QueueUpdated => deliver()

    case Cancel => context.stop(self)
  }

  @tailrec
  final def deliver(): Unit = {

    if (queue.size == 0 && totalDemand != 0) {
      queueUpdated = false
    } else if (totalDemand > 0 && queue.size > 0) {
      onNext(queue.dequeue())
      deliver()
    }
  }
}
