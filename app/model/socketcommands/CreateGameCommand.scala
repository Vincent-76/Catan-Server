package model.socketcommands

import controllers.SessionController
import model.form.NewGame
import model.{ GameData, GameSession, SocketCommandScope, TypedSocketCommand }
import play.api.libs.json.JsValue

import scala.util.{ Success, Try }

/**
 * @author Vincent76
 */

object CreateGameCommand extends TypedSocketCommand[NewGame]( "createGame", NewGame.form ) {
  override def typedExecute( sessionController:SessionController, sessionID:String, gameSession:Option[GameSession], data:NewGame ):Try[JsValue] = {
    sessionController.deleteGameSession( sessionID )
    val module = data.module.instance( fileIO = data.fileIO, availablePlacements = data.availablePlacements )
    val newGameSession:GameSession = sessionController.newGameSession( sessionID, module )
    Success( GameData( sessionID, newGameSession ).toJson )
  }
}
