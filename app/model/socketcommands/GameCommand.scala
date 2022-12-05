package model.socketcommands

import model.{ GameData, GameSession, SocketCommand }
import play.api.libs.json.JsValue

import scala.util.{ Success, Try }

/**
 * @author Vincent76
 */

object GameCommand extends SocketCommand( "Game" ) {

  override def execute( gameSession:GameSession ):Try[JsValue] =
    Success( gameSession.controller.game.toJson )

}
