package bullrush.yahoofinance

case class Estimates(EPSEstimateCurrentYear: Option[String],
                     EPSEstimateNextYear: Option[String],
                     EPSEstimateNextQuarter: Option[String],
                     OneyrTargetPrice: Option[String]
                      )
