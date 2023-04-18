package model.socketcommands

import model.{ GameSession, GameSocketCommand, GameStatus, SocketCommandScope }
import play.api.libs.json.{ JsValue, Json }

import scala.util.{ Success, Try }

/**
 * @author Vincent76
 */

object GameStatusCommand extends GameSocketCommand( "gameStatus", SocketCommandScope.Always ) {

  override def gameExecute( gameSession:GameSession, sessionID:String, data:String ):Try[JsValue] =
    Success( Json.toJson( GameStatus( gameSession.controller ) ) )

}
