package bullrush.server

import java.util.UUID
import java.util.concurrent.TimeoutException

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpMethods._
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.ws._
import akka.stream.ActorMaterializer
import akka.stream.scaladsl._
import bullrush.model.{TickerUpdate, SocketEvent, TickerDetails}
import bullrush.server.RouterActor.SendStats
import bullrush.server.TickerActor.SubscribeToTicker
import spray.json._
import TickerModelProtocal._

import scala.concurrent.Await
import scala.concurrent.duration._

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

case object CommunicationProtocols extends DefaultJsonProtocol {
  implicit val tickerUpdateFormat = jsonFormat3(TickerUpdate.apply)
  implicit val socketEventFormat = jsonFormat3(SocketEvent.apply)
}


object ChatServer extends App {

  implicit val system = ActorSystem("chatHandler")
  implicit val fm = ActorMaterializer()
  import CommunicationProtocols._

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

  try {
    Await.result(binding, 1 second)
    println("Server online at http://localhost:9001")
  } catch {
    case exc: TimeoutException =>
      println("Startup took too long, shutting down")
      system.shutdown()
  }

}


