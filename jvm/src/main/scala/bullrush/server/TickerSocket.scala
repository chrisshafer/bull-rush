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
import bullrush.model._
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
  implicit val messageFormat = jsonFormat2(MessageDetails.apply)
  implicit val tickerUpdateFormat = jsonFormat4(TickerMessage.apply)
}


object TickerServer extends App {

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
      import upickle.default._

      val clientId = UUID.randomUUID().toString
      val source = Source.actorPublisher[String](Props(classOf[RouterPublisher],router,clientId))
      val merge = b.add(Merge[String](2))

      val validOrInvalid = b.add(Flow[TickerMessage].map{

        case register: TickerMessage if register.code == 201 =>
          tickerActor ! SubscribeToTicker(register.message,clientId)
          TickerMessage("Subscribed to : "+register.message,201,None,None).toJson.toString()
        case _ =>
          TickerMessage("Invalid Message",500,None,None).toJson.toString()
      })

      val mapMsgToIncomingMessage = b.add(Flow[Message].map[TickerMessage] {
        case TextMessage.Strict(txt) => read[TickerMessage](txt)
        case _ => TickerMessage("",-1,None,None)
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


