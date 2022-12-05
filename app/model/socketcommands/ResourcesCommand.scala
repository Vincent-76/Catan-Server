package model.socketcommands

import model.{ GameSession, SocketCommand }
import play.api.libs.json.{ JsValue, Json }

import scala.util.{ Success, Try }

/**
 * @author Vincent76
 */

object ResourcesCommand extends SocketCommand( "Resources" ) {

  override def execute( gameSession:GameSession ):Try[JsValue] =
    Success( Json.toJson( gameSession.controller.game.resourceCards ) )

}
