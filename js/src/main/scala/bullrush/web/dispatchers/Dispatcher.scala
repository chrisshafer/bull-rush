package bullrush.web.dispatchers

class Dispatcher[A] {

  private val subscribers : scala.collection.mutable.Map[Int, A => Unit] = scala.collection.mutable.Map()
  private var count = 0

  def dispatch(message: A): Unit = {
    subscribers.foreach( _._2(message))
  }

  def register(handler: A => Unit): Int = {
    count += 1
    subscribers += ( count -> handler)
    count
  }

  def deregister(key: Int): Unit ={
    subscribers -= key
  }

}
