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
        $.modState(_.copy(indexes = filtered, dailyPercentage = TickerStore.getDailyPercentChange))
      }
    }

  }
  val curFmt = "%.2f"

  def toIndicatedPercent(change: Double): String ={
    val indicator : String = if(change < 0) "-" else if(change > 0) "+" else ""
    indicator+change.formatted("%.2f")+"%"
  }
  def getShortName(ticker: String): String = {
    val mapping = Map( "^IXIC" -> "Nasdaq" , "^GSPC" -> "S&P 500" )
    if(mapping.contains(ticker)){
      mapping(ticker)
    } else ticker
  }
  def stat(name: String, change: Double, percentChange: String) = {
    val color : String = if(change < 0) "negative" else if(change > 0) "positive" else ""
    <.div(
      <.span( ^.className := "index-short",name),
      <.span( ^.className := "index-change "+color,percentChange)
    )
  }
  def tickerChange(ticker: TickerDetails) = {
    val change: Double = ticker.stats.change.getOrElse(0)
    stat(getShortName(ticker.stats.ticker.getOrElse("")),
      ticker.stats.change.getOrElse(0),
      ticker.stats.percentChange.getOrElse("0.0%"))
  }
  val component = ReactComponentB[Unit]("DetailHeader")
    .initialState(new State(TickerStore.getTickers, 0, TickerStore.getDailyPercentChange))
    .backend(new Backend(_))
    .render { (_, S, B) =>

      <.div( ^.className := "detail-header",
        <.div( ^.className := "stats",
          S.indexes.map(tickerChange),
          stat("Watching",S.dailyPercentage,toIndicatedPercent(S.dailyPercentage))
        ),
        <.div( ^.className := "controls")
      )

    }
    .componentDidMount( c => c.backend.onLoad())
    .buildU
}
