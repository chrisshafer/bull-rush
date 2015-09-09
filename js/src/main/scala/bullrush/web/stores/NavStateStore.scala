package bullrush.web.stores

import bullrush.web.actions.NavStateActions.{SetCurrentTicker, SetCurrentPage, SwitchTicker}
import bullrush.web.actions.ReceiveTickerUpdate
import bullrush.web.dispatchers._
import bullrush.web.fluxutil.ChangeEventEmitter

object NavStateStore extends ChangeEventEmitter{

  def init(): Unit ={}
  def cleanStore() = {}

  private var currentTicker : String = ""
  private var currentPage : String = ""

  def getCurrentTicker = currentTicker
  def getCurrentPage = currentPage

  val dispatchId = NavStateDispatcher.register({
    case SwitchTicker(ticker) =>
      println("SWITCH TICKER NEEDS TO BE IMPLEMENTED")
      emitChange()

    case SetCurrentPage(page) =>
      currentPage = page
      println("Switching to "+page)
      emitChange()

    case SetCurrentTicker(ticker) =>
      currentTicker = ticker
      emitChange()

  })

}
