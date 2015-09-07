package bullrush.web.components

import bullrush.web.app.MainRouter.{SingleTickerPage, TickerGridPage, NotFoundPage, MainPages}
import japgolly.scalajs.react.extra.router2.RouterCtl
import japgolly.scalajs.react.vdom.ReactTag
import japgolly.scalajs.react.{ReactComponentB, BackendScope}
import japgolly.scalajs.react.vdom.prefix_<^._

object SideNav {

  case class Props( ctl : Option[RouterCtl[MainPages]])
  case class State( ctl : RouterCtl[MainPages])
  class Backend($: BackendScope[Props, State]){
    def navStateChange(): Unit ={

    }
  }

  val component = ReactComponentB[Props]("LeftNav")
    .getInitialState( props => new State( props.ctl.get))
    .backend(new Backend(_))
    .render{ (P,S,B) =>

    def navElement(icon: String, target: MainPages, current: Boolean) = {
      val currentStyle = if(current) ^.className := "current" else ^.className := ""
      <.li(
        currentStyle,
        <.i(^.className := icon),
        S.ctl setOnClick target)
    }

    <.div(
      ^.className := "sidebar-menu",
      <.ul(
        <.li(<.img( ^.src := "/resources/img/bull-smal.png")),
        navElement("fa fa-usd fa-lg", TickerGridPage, false),
        navElement("fa fa-line-chart fa-lg", SingleTickerPage("SUNE"), true ),
        navElement("fa fa-cog fa-lg", NotFoundPage, false)
      )
    )

  }.componentDidMount(c => {

  }).build

}
