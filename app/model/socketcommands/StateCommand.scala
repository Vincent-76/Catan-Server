package model.socketcommands

import model.{ GameSession, GameSocketCommand, SocketCommandScope }
import play.api.libs.json.JsValue

import scala.util.{ Success, Try }

/**
 * @author Vincent76
 */
object StateCommand extends GameSocketCommand( "state", SocketCommandScope.Always ) {

  override def gameExecute( gameSession:GameSession, sessionID:String, data:String ):Try[JsValue] =
    Success( gameSession.controller.game.state.toJson )
}
