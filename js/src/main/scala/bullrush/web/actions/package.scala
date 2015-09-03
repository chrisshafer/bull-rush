package bullrush.web

import bullrush.model.TickerDetails

package object actions {

  sealed trait TickerAction
  sealed trait StandardAction

  trait TickerUpdateAction extends TickerAction
  trait TickerRegistrationAction extends StandardAction

  case class ReceiveTickerUpdate(details: TickerDetails) extends TickerUpdateAction
  case class SubscribeToTicker(subscribe: String) extends StandardAction

}
