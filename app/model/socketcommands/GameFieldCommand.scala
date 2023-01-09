package model.socketcommands

import model.{ GameSession, SocketCommand }
import play.api.libs.json.JsValue

import scala.util.{ Success, Try }

/**
 * @author Vincent76
 */

object GameFieldCommand extends SocketCommand( "gameField" ) {

  override def exec( gameSession:GameSession, sessionID:String, data:String ):Try[JsValue] =
    Success( gameSession.controller.game.gameField.toJson )

}
