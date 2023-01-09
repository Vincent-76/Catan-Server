package model.socketcommands

import model.{ GameData, GameSession, SocketCommand }
import play.api.libs.json.JsValue

import scala.util.{ Success, Try }

/**
 * @author Vincent76
 */

object GameDataCommand extends SocketCommand( "gameData" ) {

  override def exec( gameSession:GameSession, sessionID:String, data:String ):Try[JsValue] =
    Success( GameData( sessionID, gameSession ).toJson )

}
