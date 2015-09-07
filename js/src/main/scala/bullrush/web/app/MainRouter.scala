package bullrush.web.app

import bullrush.web.components.pages.{SingleTicker, TickerGrid}
import japgolly.scalajs.react.extra.router2.{Redirect, Resolution, RouterConfigDsl, RouterCtl}
import japgolly.scalajs.react.vdom.prefix_<^._
object MainRouter {

  sealed trait MainPages

  case object TickerGridPage extends MainPages
  case class  SingleTickerPage(symbol: String) extends MainPages
  case object NotFoundPage extends MainPages

  object MainPages {

    val router = RouterConfigDsl[MainPages].buildConfig { dsl =>
      import dsl._
      (trimSlashes
        | staticRoute(root, TickerGridPage) ~> render(TickerGrid.component())
        | dynamicRouteCT("#ticker" / string("[A-Za-z0-9-]{1,25}").caseClass[SingleTickerPage]) ~> {
            dynRender(SingleTicker.component(_))
        }
        | staticRoute("#404", NotFoundPage) ~> render(<.div("TODO"))
        ).notFound(redirectToPage(TickerGridPage)(Redirect.Replace))
        .renderWith(layout)

    }


    def layout(c: RouterCtl[MainPages], r: Resolution[MainPages]) = {
      BullRushApp.mainRouter = Option(c)
      <.div(
        <.div(
            ^.className := "sidebar-menu",
            <.ul(
              <.li(<.img( ^.src := "/resources/img/bull-smal.png")),
              <.li(<.i(^.className := "fa fa-usd fa-lg")),
              <.li(<.i(^.className := "fa fa-line-chart fa-lg")),
              <.li(<.i(^.className := "fa fa-cog fa-lg"))
            )
        ),
        <.div(
          ^.className := "master-container",
          <.div(
            r.render()
          )
        )
      )
    }
  }
}
