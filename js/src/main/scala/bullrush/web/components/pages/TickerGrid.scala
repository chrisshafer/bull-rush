package bullrush.web.components.pages

import bullrush.model.TickerDetails
import bullrush.web.stores.TickerStore
import japgolly.scalajs.react.vdom.prefix_<^._
import japgolly.scalajs.react.{BackendScope, ReactComponentB}

object TickerGrid {

  case class State(tickers: Seq[TickerDetails])
  class Backend($: BackendScope[Unit, State]){

    def onLoad() = {
      TickerStore.addChangeListener( () => onTickerChange())
    }

    def onTickerChange() = {
      if($.isMounted()){
        $.modState(_.copy(tickers = TickerStore.getTickers))
      }
    }

  }

  def detail (key: String, value: String) = {<.div( ^.className := "row",
    <.span( ^.className := "detail", key + " : " + value)
  )
  }

  def tickerCard(ticker: TickerDetails) ={
    <.li(
      <.div(^.className := "ticker-card",
        <.div( ^.className := "small-6 columns",
          <.span( ^.className := "ticker", ticker.stats.ticker),
          <.div( ^.className := "details",
            detail("50 Day Avg.", ticker.stats.fiftyDayMovingAverage.get.toString),
            detail("Year High", ticker.HighLow.yearHigh.get.toString),
            detail("Year Low", ticker.HighLow.yearLow.get.toString)
          )
        ),
        <.div( ^.className := "small-6 columns",
          <.span( ^.className := "price row", ticker.stats.lastPrice.get),
          <.span( ^.className := "change row", ticker.stats.change.get)
        )
      )
    )
  }
  val component = ReactComponentB[Unit]("TickerGrid")
    .initialState(new State(Seq()))
    .backend(new Backend(_))
    .render { (_, S, B) =>

    <.ul(
      ^.className := "small-block-grid-1 medium-block-grid-2 large-block-grid-3",
      S.tickers.map(ticker => tickerCard(ticker))
    )

  }
    .componentDidMount( c => c.backend.onLoad())
    .buildU

}
