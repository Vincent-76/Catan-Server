package model.socketcommands

import model._
import play.api.libs.json.JsValue

import scala.util.Try

/**
 * @author Vincent76
 */
object RollTheDicesCommand extends GameSocketCommand( "rollTheDices", SocketCommandScope.Turn ) {

  override def gameExecute( gameSession:GameSession, sessionID:String, data:String ):Try[JsValue] =
    controllerAction( gameSession, sessionID, _.action( _.rollTheDices() ) )
}
