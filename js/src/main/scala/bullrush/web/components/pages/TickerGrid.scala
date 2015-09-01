package bullrush.web.components.pages

import japgolly.scalajs.react.vdom.prefix_<^._
import japgolly.scalajs.react.{BackendScope, ReactComponentB}

object TickerGrid {

  case class State()
  class Backend($: BackendScope[Unit, State]){

    def onLoad() = {

    }

  }

  val component = ReactComponentB[Unit]("TickerGrid")
    .initialState(new State())
    .backend(new Backend(_))
    .render { (_, S, B) =>

    <.h1("HELLO WORLD")
  }
    .componentDidMount( c => c.backend.onLoad())
    .buildU

}
