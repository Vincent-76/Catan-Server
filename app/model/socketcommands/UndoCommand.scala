package model.socketcommands

import model._
import play.api.libs.json.JsValue

import scala.util.Try

/**
 * @author Vincent76
 */
object UndoCommand extends GameSocketCommand( "undo", SocketCommandScope.Turn ) {

  override def gameExecute( gameSession:GameSession, sessionID:String, data:String ):Try[JsValue] =
    controllerAction( gameSession, sessionID, _.undoAction(), undo = true )
}
