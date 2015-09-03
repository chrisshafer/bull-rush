package bullrush.server

import akka.actor.{Actor, ActorRef, Props}
import akka.routing._
import akka.stream.actor.ActorPublisher
import bullrush.model.{TickerMessage, TickerDetails}
import bullrush.server.RouterActor.{ AddClient, UpdateClients, RemoveClient}
import spray.json._
import TickerModelProtocal._
import scala.annotation.tailrec
import upickle.default._

class RouterActor extends Actor {
  private var clients = Map[String,Routee]()
  import CommunicationProtocols._

  def receive = {
    case AddClient(routee,id) =>
      clients = clients + (id -> routee)

    case RemoveClient(routee,id) =>
      clients = clients - id

    case UpdateClients(details, torecieve) =>
      val message = TickerMessage("",202,Some(details),None)
      println("Sending to : "+clients.filterKeys(torecieve))
      clients.filterKeys(torecieve).foreach(_._2.send(write(message),sender))

    case msg: TickerMessage =>
      clients.values.foreach(_.send(msg.toJson.toString() ,sender))

    case tickers: Seq[TickerDetails] =>
      clients.values.foreach(_.send(tickers.toJson.toString(), sender))

    case msg => clients.values.foreach(_.send(msg, sender))
  }
}
object RouterActor{
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