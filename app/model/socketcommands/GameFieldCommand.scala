package model.socketcommands

import model.{ GameSession, GameSocketCommand, SocketCommand, SocketCommandScope }
import play.api.libs.json.JsValue

import scala.util.{ Success, Try }

/**
 * @author Vincent76
 */

object GameFieldCommand extends GameSocketCommand( "gameField", SocketCommandScope.Always ) {

  override def gameExecute( gameSession:GameSession, sessionID:String, data:String ):Try[JsValue] =
    Success( gameSession.controller.game.gameField.toJson )

}
