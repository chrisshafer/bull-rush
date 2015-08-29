import spray.json._
import spray.httpx.SprayJsonSupport._
import spray.json._
import DefaultJsonProtocol._

case class TickerDetails(symbol: String)
object SymbolJsonProtocol extends DefaultJsonProtocol {

  implicit object TickerDetailCollectionFormat extends RootJsonFormat[Seq[TickerDetails]] {
    def write(c: Seq[TickerDetails]) = JsObject(
      "symbol" -> JsString("dont use this")
    )
    def read(value: JsValue) = { // this is an unholy mess
      value.asJsObject.fields.get("query").get.asJsObject
        .fields.get("results").get.asJsObject
        .fields.get("quote").get.asInstanceOf[JsArray].elements.map{
        x => x.asJsObject.getFields("symbol") match {

          case Seq(JsString(symbol)) =>
            TickerDetails(symbol)
          case _ => throw new DeserializationException("Complex expected")
        }
      }
    }
  }
  implicit object TickerDetailFormat extends RootJsonFormat[TickerDetails] {
    def write(c: TickerDetails) = JsObject(
      "symbol" -> JsString("dont use this")
    )
    def read(value: JsValue) = { // this is an unholy mess
      value.asJsObject.fields.get("query").get.asJsObject
        .fields.get("results").get.asJsObject.fields.get("quote").get.asJsObject
        .getFields("symbol") match {

        case Seq(JsString(symbol)) =>
          TickerDetails(symbol)
        case _ => throw new DeserializationException("Complex expected")
      }
    }

  }
}
