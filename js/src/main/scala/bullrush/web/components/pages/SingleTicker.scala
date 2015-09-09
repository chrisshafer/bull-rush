package bullrush.web.components.pages

import bullrush.model.TickerDetails
import bullrush.web.actions.NavStateActions
import bullrush.web.app.MainRouter.SingleTickerPage
import bullrush.web.stores.TickerStore
import japgolly.scalajs.react.vdom.prefix_<^._
import japgolly.scalajs.react.{ReactComponentB, ReactNode, BackendScope}

object SingleTicker {
  case class State(ticker: Option[TickerDetails])
  class Backend($: BackendScope[SingleTickerPage, State]){

    def onLoad() = {
      NavStateActions.setCurrentPage("SingleTickerPage")
      TickerStore.addChangeListener( () => onTickerChange())
    }

    def onTickerChange() = {
      if($.isMounted()){
        $.modState(_.copy(ticker = Some(TickerStore.getTicker($.props.symbol))))
      }
    }

  }
  val curFmt = "%.2f"

  def details(ticker: TickerDetails): ReactNode = {

    <.div( ^.className := "details",
      detail("50 Day Avg", ticker.stats.fiftyDayMovingAverage.get.toString),
      detail("52 week", ticker.HighLow.yearLow.get.formatted(curFmt) +" - "+ticker.HighLow.yearHigh.get.formatted(curFmt)),
      detail("Mkt Cap", ticker.stats.marketCap.get),
      detail("P/E", if(ticker.ratios.peRatio.isDefined) ticker.ratios.peRatio.get.formatted(curFmt)  else " - " ),
      detail("EPS", ticker.ratios.earningsPerShare.get.formatted(curFmt)),
      detail("Range", ticker.HighLow.dayLow.get.formatted(curFmt)  + " - " + ticker.HighLow.dayHigh.get.formatted(curFmt))
    )
  }
  def detail (key: String, value: String) = {<.div( ^.className := "row",
    <.span( ^.className := "detail",
      <.span( ^.className := "detail-key", key ),
      <.span( ^.className := "detail-value", value ))
  )
  }
  def priceComponent (price: Double, change: Double, percentChange: String) ={
    val color : String = if(change < 0) "negative" else if(change > 0) "positive" else ""
    <.div( ^.className := "price-display",
      <.span( ^.className := "change row "+color, change.formatted(curFmt)),
      <.span( ^.className := "price row ", price.formatted(curFmt)),
      <.span( ^.className := "change row "+color, percentChange)
    )
  }

  def indicatorColor(change: Double) = if(change < 0) "negative" else if(change > 0) "positive" else "no-change"

  val component = ReactComponentB[SingleTickerPage]("TickerGrid")
    .initialState(new State(None))
    .backend(new Backend(_))
    .render { (_, S, B) =>

    <.ul(
      ^.className := "small-block-grid-1 medium-block-grid-2 large-block-grid-3",
      if(S.ticker.isDefined) details(S.ticker.get) else <.span("ERROR")
    )

  }
    .componentDidMount( c => c.backend.onLoad())
    .build

}
