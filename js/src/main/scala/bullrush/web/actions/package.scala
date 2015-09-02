package bullrush.web

package object actions {

  sealed trait TickerAction
  sealed trait StandardAction

  trait TickerUpdateAction extends TickerAction
  trait TickerRegistrationAction extends TickerAction

}
