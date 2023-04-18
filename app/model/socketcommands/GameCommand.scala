package model.socketcommands

import model.{ GameSession, GameSocketCommand, SocketCommand, SocketCommandScope }
import play.api.libs.json.JsValue

import scala.util.{ Success, Try }

/**
 * @author Vincent76
 */

object GameCommand extends GameSocketCommand( "game", SocketCommandScope.Always ) {

  override def gameExecute( gameSession:GameSession, sessionID:String, data:String ):Try[JsValue] =
    Success( gameSession.controller.game.toJson )

}
