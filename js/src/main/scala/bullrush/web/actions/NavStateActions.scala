package bullrush.web.actions

import bullrush.web.app.BullRushApp
import bullrush.web.app.MainRouter.MainPages
import bullrush.web.dispatchers.NavStateDispatcher
object NavStateActions {

  sealed trait NavStateAction

  trait PageStateAction extends NavStateAction
  trait TickerStateAction extends NavStateAction

  case class SwitchTicker(ticker: String) extends TickerStateAction

  case class SetCurrentPage(page: String) extends PageStateAction
  case class SetCurrentTicker(ticker: String) extends TickerStateAction

  def switchTicker(stax: String): Unit = {
    NavStateDispatcher.dispatch(SwitchTicker(stax))
    BullRushApp.mainRouter match {
      case Some(ctl) =>
        println("ERR, NOT IMPLEMENTED")
      case _ =>
        println("Err, router is not set")
    }
  }

  def setCurrentTicker(stax: String): Unit = {
    NavStateDispatcher.dispatch(SetCurrentTicker(stax))
  }

  def setCurrentPage(name: String): Unit = {
    println(name)
    NavStateDispatcher.dispatch(SetCurrentPage(name))
  }


}
