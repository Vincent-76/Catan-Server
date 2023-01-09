package model.socketcommands

import model.{ GameSession, SocketCommand }
import play.api.libs.json.{ JsValue, Json }

import scala.util.{ Success, Try }

/**
 * @author Vincent76
 */

object TurnCommand extends SocketCommand( "turn" ) {

  override def exec( gameSession:GameSession, sessionID:String, data:String ):Try[JsValue] =
    Success( Json.toJson( gameSession.controller.game.turn ) )

}
