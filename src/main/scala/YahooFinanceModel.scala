import spray.json._
import spray.httpx.SprayJsonSupport._
import spray.json._
import DefaultJsonProtocol._

case class TickerDetails(symbol: String)
case class YahooTickerDetails(symbol: String){
  def toTickerDetails: TickerDetails ={
    TickerDetails(symbol)
  }
}
object SymbolJsonProtocol extends DefaultJsonProtocol {

  implicit val tickerDetailsFormat = jsonFormat1(TickerDetails.apply)

  implicit object YahooTickerDetailsCollectionFormat extends RootJsonFormat[Seq[YahooTickerDetails]] {
    def write(c: Seq[YahooTickerDetails]) = JsObject(
      "symbol" -> JsString("dont use this")
    )
    def read(value: JsValue) = { // this is an unholy mess
      value.asJsObject.fields.get("query").get.asJsObject
        .fields.get("results").get.asJsObject
        .fields.get("quote").get.asInstanceOf[JsArray].elements.map{
        x => x.asJsObject.getFields("symbol") match {

          case Seq(JsString(symbol)) =>
            YahooTickerDetails(symbol)
          case _ => throw new DeserializationException("Complex expected")
        }
      }
    }
  }
  implicit object TickerDetailFormat extends RootJsonFormat[YahooTickerDetails] {
    def write(c: YahooTickerDetails) = JsObject(
      "symbol" -> JsString("dont use this")
    )
    def read(value: JsValue) = { // this is an unholy mess
      value.asJsObject.fields.get("query").get.asJsObject
        .fields.get("results").get.asJsObject.fields.get("quote").get.asJsObject
        .getFields("symbol") match {

        case Seq(JsString(symbol)) =>
          YahooTickerDetails(symbol)
        case _ => throw new DeserializationException("Complex expected")
      }
    }

  }
}
