package model.socketcommands

import model.{ GameSession, GameSocketCommand, SocketCommand, SocketCommandScope }
import play.api.libs.json.JsValue

import scala.util.Try

/**
 * @author Vincent76
 */
object DiceOutBeginnerCommand extends GameSocketCommand( "diceOutBeginner", SocketCommandScope.Host ) {
  override def gameExecute( gameSession:GameSession, sessionID:String, data:String ):Try[JsValue] = {
    controllerAction( gameSession, sessionID, _.action( _.diceOutBeginner() ) )
  }
}
