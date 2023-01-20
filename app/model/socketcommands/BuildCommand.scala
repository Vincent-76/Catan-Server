package model.socketcommands

import model.{ GameSession, GameSocketCommand, InvalidInput, SocketCommandScope }
import play.api.libs.json.JsValue

import scala.util.{ Failure, Try }

/**
 * @author Vincent76
 */
object BuildCommand extends GameSocketCommand( "build", SocketCommandScope.Turn ) {
  override def gameExecute( gameSession:GameSession, sessionID:String, data:String ):Try[JsValue] =
    data.toIntOption
      .map( id => controllerAction( gameSession, _.action( _.build( id ) ) ) )
      .getOrElse( Failure( InvalidInput( data ) ) )
}
