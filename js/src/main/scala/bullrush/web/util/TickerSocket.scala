package bullrush.web.util

import bullrush.model.{MessageDetails, TickerDetails, TickerMessage}
import org.scalajs.dom
import org.scalajs.dom.raw.{MessageEvent, ErrorEvent, CloseEvent, Event}
import upickle.{Js, Invalid}
import upickle.default._

class TickerSocket(url: String, handler: (TickerMessage) => (Unit)) {

  private val CONNECTING = 0
  private val OPEN = 1
  private val CLOSING = 2
  private val CLOSED = 3

  val websocket = new dom.WebSocket(url)

  private var messageQueue: Seq[TickerMessage] = Seq()

  websocket.onclose = (event: CloseEvent) => println("Socket Closed : "+event.reason)
  websocket.onerror = (event: ErrorEvent) => println("Socket Error : "+event.message)
  websocket.onopen  = (event: Event)      => {
    println("Socket Opened: "+event.timeStamp)
    messageQueue.foreach(send)
  }

  websocket.onmessage = (event: MessageEvent) => {
    handler(read[TickerMessage](event.data.toString))
  }

  def close() = websocket.close()
  def send(message: TickerMessage) = {
    websocket.readyState match {
      case OPEN =>
        println("SENDING : "+write(message))
        websocket.send(write(message))
      case CONNECTING =>
        messageQueue = messageQueue :+ message
      case CLOSED =>
        println("ERROR SOCKET CLOSED")
      case CLOSING =>
        println("ERROR SOCKET CLOSING")
    }
  }

}
