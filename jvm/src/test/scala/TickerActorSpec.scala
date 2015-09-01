import akka.actor.{ActorRef, ActorSystem, Props}
import akka.util.Timeout
import bullrush.{TickerDetails, RouterActor, TickerActor}
import bullrush.TickerActor.{GetTicker, SubscribeToTicker}
import org.scalatest.concurrent.{Eventually, ScalaFutures}
import org.scalatest.time.{Seconds, Span}
import org.scalatest.{FunSpec, ShouldMatchers}

import scala.concurrent.duration._


class TickerActorSpec extends FunSpec with ShouldMatchers with Eventually with ScalaFutures{
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
    tickerActor ! SubscribeToTicker(stockTicker,"test")

    it("Will retrive the details for a stock ticker"){

      eventually{
        val future = (tickerActor ? GetTicker(stockTicker)).mapTo[TickerDetails]
        whenReady(future) { response =>
          assert(response.stats.ticker.get == stockTicker)
        }
      }

    }

  }
}