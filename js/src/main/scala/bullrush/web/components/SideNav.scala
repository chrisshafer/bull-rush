package bullrush.web.components

import bullrush.web.app.MainRouter.{SingleTickerPage, TickerGridPage, NotFoundPage, MainPages}
import bullrush.web.stores.NavStateStore
import japgolly.scalajs.react.extra.router2.RouterCtl
import japgolly.scalajs.react.vdom.ReactTag
import japgolly.scalajs.react.{ReactComponentB, BackendScope}
import japgolly.scalajs.react.vdom.prefix_<^._

object SideNav {

  case class Props( ctl : Option[RouterCtl[MainPages]])
  case class State( ctl : RouterCtl[MainPages], currentPage: String)
  class Backend($: BackendScope[Props, State]){
    def navStateChange(): Unit ={
      $.modState(_.copy(currentPage = NavStateStore.getCurrentPage))
    }
    def onLoad(): Unit ={
      NavStateStore.addChangeListener(() => {
        navStateChange()
      })
    }
  }

  val component = ReactComponentB[Props]("LeftNav")
    .getInitialState( props => new State( props.ctl.get, NavStateStore.getCurrentPage))
    .backend(new Backend(_))
    .render{ (P,S,B) =>

    def isCurrent(page: String): Boolean = page == S.currentPage

    def navElement(icon: String, target: MainPages, isCurrentPage: Boolean) = {
      val currentStyle = if(isCurrentPage) ^.className := "current" else ^.className := ""
      <.li(
        currentStyle,
        <.i(^.className := icon),
        S.ctl setOnClick target)
    }
    println(S.currentPage +" vs "+ NavStateStore.getCurrentPage)
    <.div(
      ^.className := "sidebar-menu",
      <.ul(
        <.li(<.img( ^.src := "/resources/img/bull-smal.png")),
        navElement("fa fa-usd fa-lg", TickerGridPage, isCurrent("TickerGridPage") ),
        navElement("fa fa-line-chart fa-lg", SingleTickerPage("SUNE"), isCurrent("SingleTickerPage") ),
        navElement("fa fa-cog fa-lg", NotFoundPage, isCurrent("NotFoundPage") )
      )
    )

  }.componentDidMount(c => {
    c.backend.onLoad()
  }).build

}
