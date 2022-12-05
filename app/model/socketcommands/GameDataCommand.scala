package model.socketcommands

import model.{ GameData, GameSession, SocketCommand }
import play.api.libs.json.JsValue

import scala.util.{ Success, Try }

/**
 * @author Vincent76
 */

object GameDataCommand extends SocketCommand( "GameData" ) {

  override def execute( gameSession:GameSession ):Try[JsValue] =
    Success( GameData( gameSession ).toJson )

}
