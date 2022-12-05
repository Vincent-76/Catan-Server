package model.socketcommands

import model.{ GameSession, SocketCommand }
import play.api.libs.json.JsValue

import scala.util.{ Success, Try }

/**
 * @author Vincent76
 */

object GameFieldCommand extends SocketCommand( "GameField" ) {

  override def execute( gameSession:GameSession ):Try[JsValue] =
    Success( gameSession.controller.game.gameField.toJson )

}
