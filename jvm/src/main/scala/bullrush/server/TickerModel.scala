package bullrush.server

import bullrush._
import bullrush.model._
import spray.json._
import spray.httpx.SprayJsonSupport._
import DefaultJsonProtocol._

object TickerModelProtocal extends DefaultJsonProtocol {
  implicit val estimateFormat = jsonFormat4(Estimates.apply)
  implicit val ratiosFormat = jsonFormat6(Ratios.apply)
  implicit val highLowFormat = jsonFormat4(HighLow.apply)
  implicit val statsLowFormat = jsonFormat20(Stats.apply)
  implicit val tickerDetailsFormat = jsonFormat4(TickerDetails.apply)

}

