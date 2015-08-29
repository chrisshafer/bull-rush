import bullrush.{RouterActor, TickerActor, TickerDetails}
import TickerActor.{GetTicker, AddTicker}
import akka.actor.{Props, ActorSystem, ActorRef}
import akka.util.Timeout
import org.scalatest.concurrent.{ScalaFutures, Eventually}
import org.scalatest.time.{Seconds, Span}
import org.scalatest.{ShouldMatchers, FunSpec}
import spray.http.StatusCodes
import bullrush.yahoofinance.SymbolJsonProtocol
import scala.concurrent.duration._
import scala.concurrent.Await
import spray.httpx.SprayJsonSupport._
import spray.json._
import DefaultJsonProtocol._


class TickerActorSpec extends FunSpec with ShouldMatchers with Eventually with ScalaFutures{
  import scala.concurrent.ExecutionContext.Implicits.global
  import SymbolJsonProtocol._
  import akka.pattern.ask

  val stockTicker = "SPWR"
  val stockTickers = Seq("SPWR","SUNE")

  implicit val timeout = Timeout(10 seconds)
  implicit val system = ActorSystem("awsPricingClientSpec")
  implicit val defaultPatience =
    PatienceConfig(timeout =  Span(5, Seconds), interval = Span(1, Seconds))

  val router = system.actorOf(Props[RouterActor], "router")
  val tickerActor : ActorRef = system.actorOf(TickerActor.props(router))

  describe("The Ticker Actor"){
    tickerActor ! AddTicker(stockTicker)

    it("Will retrive the details for a stock ticker"){

      eventually{
        val future = (tickerActor ? GetTicker(stockTicker)).mapTo[TickerDetails]
        whenReady(future) { response =>
          assert(response.symbol == stockTicker)
        }
      }

    }

  }
}