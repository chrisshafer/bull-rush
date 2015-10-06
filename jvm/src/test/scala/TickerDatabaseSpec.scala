import bullrush.server.database.TickerDatabase
import bullrush.server.yahoofinance.YahooFinanceClient
import org.scalatest._
import org.scalatest.concurrent.Eventually
import scala.concurrent.duration._

import scala.concurrent.Await

class TickerDatabaseSpec extends FunSpec with ShouldMatchers with Eventually{

  Await.result(TickerDatabase.populateDBDDLIfNonexistant(),10 seconds)
  describe("The ticker database"){
    it("Should insert a user"){
      Await.result(TickerDatabase.addUser("Chris","Chris","Chrismail"),3 seconds)
     println(Await.result(TickerDatabase.listUsers(), 5 seconds))
    }
    it("Should contain users"){

    }


  }
}
