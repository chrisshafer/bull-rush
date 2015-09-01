package bullrush.web.fluxutil;

class ChangeEventEmitter extends EventEmitter {

  val CHANGE_EVENT = "change"

  def emitChange() { emit(CHANGE_EVENT) }

  def addChangeListener(cb: () => Unit) { on(CHANGE_EVENT, cb) }

  def removeChangeListener(cb: () => Unit) { removeListener(CHANGE_EVENT, cb) }

}
