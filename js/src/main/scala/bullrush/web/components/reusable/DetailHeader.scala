package bullrush.web.components.reusable

import bullrush.model.TickerDetails
import bullrush.web.actions.NavStateActions
import bullrush.web.stores.TickerStore
import japgolly.scalajs.react.{BackendScope, ReactComponentB}
import japgolly.scalajs.react.vdom.prefix_<^._

object DetailHeader {

  case class State(indexes: Seq[TickerDetails], dailyChange: Double, dailyPercentage: Double)

  private val indexSymbols = Seq("^IXIC","^GSPC")
  class Backend($: BackendScope[Unit, State]){

    def onLoad() = {
      TickerStore.addChangeListener( () => onTickerChange())
    }

    def onTickerChange() = {
      if($.isMounted()){
        val filtered = TickerStore.getTickers.filter( ticker => indexSymbols.contains(ticker.stats.ticker.getOrElse("")) )
        $.modState(_.copy(indexes = filtered))
      }
    }

  }
  val curFmt = "%.2f"

  def getShortName(ticker: String): String = {
    val mapping = Map( "^IXIC" -> "Nasdaq" , "^GSPC" -> "S&P 500" )
    if(mapping.contains(ticker)){
      mapping(ticker)
    } else ticker
  }
  def tickerChange(ticker: TickerDetails) = {
    val change: Double = ticker.stats.change.getOrElse(0)
    val color : String = if(change < 0) "negative" else if(change > 0) "positive" else ""
    <.div(
      <.span( ^.className := "index-short", getShortName(ticker.stats.ticker.getOrElse(""))),
      <.span( ^.className := "index-change "+color, ticker.stats.percentChange)
    )
  }
  val component = ReactComponentB[Unit]("DetailHeader")
    .initialState(new State(Seq(), 0, 0))
    .backend(new Backend(_))
    .render { (_, S, B) =>

      <.div( ^.className := "detail-header",
        <.div( ^.className := "stats",S.indexes.map(tickerChange)),
        <.div( ^.className := "controls")
      )

    }
    .componentDidMount( c => c.backend.onLoad())
    .buildU
}
