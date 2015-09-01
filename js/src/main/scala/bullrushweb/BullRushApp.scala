package bullrushweb

import bullrush.model.Estimates
import org.scalajs.dom

import scala.scalajs.js.JSApp

object BullRushApp extends JSApp {

  def main(): Unit ={
      println(Estimates(Some(0),Some(0),Some(0),Some(0)))
  }
}
