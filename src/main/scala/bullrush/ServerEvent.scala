package bullrush

import java.util.UUID

import akka.actor.{Actor, ActorRef, Props}
import akka.routing._
import akka.stream.actor.ActorPublisher
import bullrush.RouterActor.{AddClient, RemoveClient, UpdateClients, SendStats}
import bullrush.SocketEvent._
import spray.json._

import scala.annotation.tailrec

class RouterActor extends Actor {
  import TickerModelProtocal._
  private var clients = Map[String,Routee]()

  def receive = {
    case AddClient(routee,id) =>
      println("Adding Routee")
      clients = clients + (id -> routee)

    case RemoveClient(routee,id) =>
      println("Removing Routee")
      clients = clients - id

    case UpdateClients(details, torecieve) =>
      println("sending to : "+torecieve)
      clients.filterKeys(torecieve).foreach(_._2.send(details.toJson.toString(),sender))

    case msg: SocketEvent =>
      clients.values.foreach(_.send(msg.toJson.toString() ,sender))

    case tickers: Seq[TickerDetails] =>
      clients.values.foreach(_.send(tickers.toJson.toString(), sender))

    case SendStats =>
      clients.values.foreach(_.send(SocketEvent(clients.size.toString,"SERVER",2).toJson.toString(),sender))

    case msg => clients.values.foreach(_.send(msg, sender))
  }
}
object RouterActor{
  case object SendStats
  case class AddClient(routee: Routee, id: String)
  case class RemoveClient(routee: Routee, id: String)
  case class UpdateClients(ticker: TickerDetails, clients: Set[String])

  def props: Props = {
    Props(new RouterActor())
  }
}
class RouterPublisher(router: ActorRef, id: String) extends ActorPublisher[String] {

  case object QueueUpdated


  import akka.stream.actor.ActorPublisherMessage._
  import scala.collection.mutable

  private val queue = mutable.Queue[String]()
  val MaxBufferSize = 100
  var queueUpdated = false

  override def preStart(): Unit = router ! AddClient(ActorRefRoutee(self),id)
  override def postStop(): Unit = router ! RemoveClient(ActorRefRoutee(self),id)

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