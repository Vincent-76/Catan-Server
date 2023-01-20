package model.socketcommands

import controllers.SessionController
import model.{ GameData, GameSession, SocketCommand }
import play.api.libs.json.{ JsNull, JsValue }

import scala.util.{ Success, Try }

/**
 * @author Vincent76
 */

object GameDataCommand extends SocketCommand( "gameData" ) {

  override def execute( sessionController:SessionController, sessionID:String, gameSession:Option[GameSession], data:String ):Try[JsValue] =
    gameSession match {
      case Some( gameSession ) => Success( GameData( sessionID, gameSession ).toJson )
      case None => Success( JsNull )
    }

}
