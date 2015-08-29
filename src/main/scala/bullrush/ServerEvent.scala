package bullrush

import akka.actor.{Actor, ActorRef, Props}
import akka.routing.{ActorRefRoutee, AddRoutee, RemoveRoutee, Routee}
import akka.stream.actor.ActorPublisher
import bullrush.RouterActor.SendStats
import bullrush.SocketEvent._
import spray.json._

import scala.annotation.tailrec

class RouterActor extends Actor {
  import TickerModelProtocal._
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

    case tickers: Seq[TickerDetails] =>
      clients.foreach(_.send(tickers.toJson.toString(), sender))

    case SendStats =>
      clients.foreach(_.send(SocketEvent(clients.size.toString,"SERVER",2).toJson.toString(),sender))

    case msg => clients.foreach(_.send(msg, sender))
  }
}
object RouterActor{
  case object SendStats

  def props: Props = {
    Props(new RouterActor())
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