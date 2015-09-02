package bullrush.web.util

import org.scalajs.dom
import org.scalajs.dom.raw.{MessageEvent, ErrorEvent, CloseEvent, Event}
import upickle.default._

class Websocket[A](url: String, handler: (A) => (Unit)) {

  val websocket = new dom.WebSocket(url)

  websocket.onclose = (event: CloseEvent) => println("Socket Closed : "+event.reason)
  websocket.onerror = (event: ErrorEvent) => println("Socket Error : "+event.message)
  websocket.onopen  = (event: Event)      => println("Socket Opened: "+event.timeStamp)

  websocket.onmessage = (event: MessageEvent) => {
    handler(read[A](event.data.toString))
  }

  def close() = websocket.close()
  def send(message: A) = websocket.send(write(message))

}
