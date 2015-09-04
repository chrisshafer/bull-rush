package bullrush.web.app

import bullrush.web.components.pages.TickerGrid
import japgolly.scalajs.react.extra.router2.{Redirect, Resolution, RouterConfigDsl, RouterCtl}
import japgolly.scalajs.react.vdom.prefix_<^._

object MainRouter {

  sealed trait MainPages
  object MainPages {
    case object TickerGridPage extends MainPages
    case object NotFoundPage extends MainPages

    val router = RouterConfigDsl[MainPages].buildConfig { dsl =>
      import dsl._
      (trimSlashes
        | staticRoute(root, TickerGridPage) ~> render(TickerGrid.component())
        | staticRoute("404", NotFoundPage) ~> render(<.div("TODO"))
        ).notFound(redirectToPage(TickerGridPage)(Redirect.Replace))
        .renderWith(layout)

    }
    def layout(c: RouterCtl[MainPages], r: Resolution[MainPages]) = {
      BullRushApp.mainRouter = Option(c)
      <.div(
        <.div(
            ^.className := "sidebar-menu"
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
