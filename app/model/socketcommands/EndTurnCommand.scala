package model.socketcommands

import model._
import play.api.libs.json.JsValue

import scala.util.Try

/**
 * @author Vincent76
 */
object EndTurnCommand extends GameSocketCommand( "endTurn", SocketCommandScope.Turn ) {

  override def gameExecute( gameSession:GameSession, sessionID:String, data:String ):Try[JsValue] =
    controllerAction( gameSession, sessionID, _.action( _.endTurn() ) )
}
