package model.socketcommands

import model.{ GameSession, GameSocketCommand, SocketCommandScope }
import play.api.libs.json.JsValue

import scala.util.Try

/**
 * @author Vincent76
 */
object SetBeginnerCommand extends GameSocketCommand( "setBeginner", SocketCommandScope.Host ) {
  override def gameExecute( gameSession:GameSession, sessionID:String, data:String ):Try[JsValue] = {
    controllerAction( gameSession, _.action( _.setBeginner() ) )
  }
}
