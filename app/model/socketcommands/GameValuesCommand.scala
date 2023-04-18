package model.socketcommands

import model.{ GameSession, GameSocketCommand, GameValues, SocketCommand, SocketCommandScope }
import play.api.libs.json.{ JsValue, Json }

import scala.util.{ Success, Try }

/**
 * @author Vincent76
 */

object GameValuesCommand extends GameSocketCommand( "gameValues", SocketCommandScope.Always ) {

  override def gameExecute( gameSession:GameSession, sessionID:String, data:String ):Try[JsValue] =
    Success( Json.toJson( GameValues( gameSession.controller.game ) ) )

}
