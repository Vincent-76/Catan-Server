package model.socketcommands

import model.{ GameSession, GameSocketCommand, SocketCommandScope }
import play.api.libs.json.{ JsValue, Json }

import scala.util.{ Success, Try }

/**
 * @author Vincent76
 */

object ResourcesCommand extends GameSocketCommand( "resources", SocketCommandScope.Always ) {

  override def gameExecute( gameSession:GameSession, sessionID:String, data:String ):Try[JsValue] =
    Success( Json.toJson( gameSession.controller.game.resourceCards ) )

}
