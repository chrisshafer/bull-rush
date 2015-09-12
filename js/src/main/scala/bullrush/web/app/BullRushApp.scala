package bullrush.web.app

import bullrush.model.Estimates
import bullrush.web.actions.TickerActions
import bullrush.web.app.MainRouter.MainPages
import japgolly.scalajs.react.extra.router2._
import org.scalajs.dom.document

import scala.scalajs.js.JSApp

object BullRushApp extends JSApp {

  var mainRouter : Option[RouterCtl[MainPages]] = None

  // Init stores

  def main(): Unit ={

    Seq("^IXIC","^GSPC","SPWR","SUNE","MSFT","ALU","FSLR","SCTY","MON").map(TickerActions.subscribeToTicker)
    val router = Router(BaseUrl.fromWindowOrigin,MainPages.router)
    router() render document.body
  }
}
