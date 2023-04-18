package model.socketcommands

import controllers.SessionController
import model.{ GameSession, SocketCommand, SocketCommandScope }
import play.api.libs.json.{ JsValue, Json }

import scala.util.{ Success, Try }

/**
 * @author Vincent76
 */
object NewGameCommand extends SocketCommand( "newGame" ) {
  override def executeRaw( sessionController:SessionController, sessionID:String, data:String ):Try[JsValue] = {
    sessionController.deleteGameSession( sessionID )
    Success( Json.obj() )
  }
}
