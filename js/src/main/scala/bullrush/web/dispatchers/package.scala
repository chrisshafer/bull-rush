package bullrush.web

import bullrush.web.actions.TickerAction

package object dispatchers {

  val TickerDispatcher = new Dispatcher[TickerAction]
}
