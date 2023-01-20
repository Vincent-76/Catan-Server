package model.socketcommands

import model.{ GameData, GameSession, GameSocketCommand, SocketCommand, SocketCommandScope }
import play.api.libs.json.JsValue

import scala.util.{ Success, Try }

/**
 * @author Vincent76
 */

object GameDataCommand extends GameSocketCommand( "gameData", SocketCommandScope.Always ) {

  override def gameExecute( gameSession:GameSession, sessionID:String, data:String ):Try[JsValue] =
    Success( GameData( sessionID, gameSession ).toJson )

}
