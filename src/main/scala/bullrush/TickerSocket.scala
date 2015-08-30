package bullrush


import java.util.UUID
import java.util.concurrent.TimeoutException

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpMethods._
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.ws._
import akka.routing.ActorRefRoutee
import akka.stream.ActorMaterializer
import akka.stream.scaladsl._
import bullrush.RouterActor.SendStats
import bullrush.RouterPublisher
import bullrush.TickerActor.SubscribeToTicker
import spray.json._

import scala.concurrent.Await
import scala.concurrent.duration._
import TickerModelProtocal._

object Upgradeable {
  def unapply(req: HttpRequest) : Option[HttpRequest] = {
    if (req.header[UpgradeToWebsocket].isDefined) {
      req.header[UpgradeToWebsocket] match {
        case Some(upgrade) => Some(req)
        case None => None
      }
    } else None
  }
}

case class TickerUpdate(message: String, code: Int = 1, tickerDetail: TickerDetails)
case object TickerUpdate extends DefaultJsonProtocol {
  implicit val protocol = jsonFormat3(TickerUpdate.apply)
}

case class SocketEvent(message: String, user: String, code: Int = 1)
case object SocketEvent extends DefaultJsonProtocol {
  implicit val protocol = jsonFormat3(SocketEvent.apply)
}

object ChatServer extends App {

  implicit val system = ActorSystem("chatHandler")
  implicit val fm = ActorMaterializer()
  import bullrush.ChatServer.system.dispatcher

  val router = system.actorOf(Props[RouterActor], "router")
  val tickerActor : ActorRef = system.actorOf(TickerActor.props(router))

  val binding = Http().bindAndHandleSync({

    case Upgradeable(req@HttpRequest(GET, Uri.Path("/events"), _, _, _)) => upgrade(req, eventGraphFlow(router))
    case _ : HttpRequest => HttpResponse(400, entity = "Invalid websocket request")

  }, interface = "localhost", port = 9001)


  def eventGraphFlow(router: ActorRef): Flow[Message, Message, Unit] = {
    Flow() { implicit b =>
      import akka.stream.scaladsl.FlowGraph.Implicits._

      val clientId = UUID.randomUUID().toString
      val source = Source.actorPublisher[String](Props(classOf[RouterPublisher],router,clientId))
      val merge = b.add(Merge[String](2))

      val validOrInvalid = b.add(Flow[SocketEvent].map{

        case register: SocketEvent if register.code == 201 =>
          tickerActor ! SubscribeToTicker(register.message,clientId)
          SocketEvent("Subscribed to : "+register.message,"SERVER",201).toJson.toString()
        case _ =>
          SocketEvent("Invalid Message","SERVER",500).toJson.toString()
      })

      val mapMsgToIncomingMessage = b.add(Flow[Message].map[SocketEvent] {
        case TextMessage.Strict(txt) => JsonParser(txt).convertTo[SocketEvent]
        case _ => SocketEvent("","",-1)
      })

      val mapStringToMsg = b.add(Flow[String].map[Message]( x => TextMessage.Strict(x)))

      val broadcasted = b.add(source)

      mapMsgToIncomingMessage ~> validOrInvalid ~> merge
                                    broadcasted ~> merge ~> mapStringToMsg

      (mapMsgToIncomingMessage.inlet, mapStringToMsg.outlet)
    }
  }

  def upgrade(req: HttpRequest, flow: Flow[Message, Message, Unit]) = {
    req.header[UpgradeToWebsocket].get.handleMessages(flow)
  }

  system.scheduler.schedule(50 milliseconds, 10 second){
    router ! SendStats
  }

  try {
    Await.result(binding, 1 second)
    println("Server online at http://localhost:9001")
  } catch {
    case exc: TimeoutException =>
      println("Startup took too long, shutting down")
      system.shutdown()
  }

}


