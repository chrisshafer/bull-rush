package bullrush.web.components.pages

import bullrush.model.TickerDetails
import bullrush.web.actions.NavStateActions
import bullrush.web.stores.TickerStore
import japgolly.scalajs.react.vdom.prefix_<^._
import japgolly.scalajs.react.{ReactComponentB, ReactNode, BackendScope}

object TickerTable {
  case class State(tickers: Seq[TickerDetails])
  class Backend($: BackendScope[Unit, State]){

    def onLoad() = {
      NavStateActions.setCurrentPage("TickerTablePage")
      TickerStore.addChangeListener( () => onTickerChange())
    }

    def onTickerChange() = {
      if($.isMounted()){
        $.modState(_.copy(tickers = TickerStore.getTickers))
      }
    }
  }
  val curFmt = "%.2f"

  def tableHeader = {
    <.thead(
      <.tr(
        <.th("Ticker"),
        <.th("Price"),
        <.th("Change"),
        <.th("% Change"),
        <.th("50 Day Avg"),
        <.th("52 week"),
        <.th("Mkt Cap"),
        <.th("P/E"),
        <.th("EPS"),
        <.th("Range")
      )
    )
  }
  def tableRow(ticker: TickerDetails): ReactNode = {
    def resolve[T]( value: Option[T]): String ={
      if(value.isDefined){
        value.get.toString
      }else " - "
    }
    <.tr(
      <.td(resolve(ticker.stats.ticker)),
      <.td(resolve(ticker.stats.lastPrice)),
      <.td(^.className := indicatorColor(ticker.stats.change.getOrElse(0)) ,resolve(ticker.stats.change)),
      <.td(^.className := indicatorColor(ticker.stats.change.getOrElse(0)) ,resolve(ticker.stats.percentChange)),
      <.td(resolve(ticker.stats.fiftyDayMovingAverage)),
      <.td(ticker.HighLow.yearLow.get.formatted(curFmt) +" - "+ticker.HighLow.yearHigh.get.formatted(curFmt)),
      <.td(resolve(ticker.stats.marketCap)),
      <.td(if(ticker.ratios.peRatio.isDefined) ticker.ratios.peRatio.get.formatted(curFmt)  else " - " ),
      <.td(if(ticker.ratios.earningsPerShare.isDefined) ticker.ratios.earningsPerShare.get.formatted(curFmt) else " - "),
      <.td(ticker.HighLow.dayLow.get.formatted(curFmt)  + " - " + ticker.HighLow.dayHigh.get.formatted(curFmt))
    )
  }

  def indicatorColor(change: Double) = if(change < 0) "negative" else if(change > 0) "positive" else "no-change"

  val component = ReactComponentB[Unit]("TickerTable")
    .initialState(new State(TickerStore.getTickers))
    .backend(new Backend(_))
    .render { (_, S, B) =>
    <.div( ^.className := "table-responsive",
      <.table(
        tableHeader,
        <.tbody(
          S.tickers.sortBy(-_.stats.percentChange.getOrElse("0").replace("%","").toDouble).map(tableRow) // Oh Lordy, change this TODO
        )
      )
    )

  }
    .componentDidMount( c => c.backend.onLoad())
    .buildU
}
