package model.socketcommands

import model.{ GameSession, GameSocketCommand, InvalidInput, SocketCommandScope }
import play.api.libs.json.JsValue

import scala.util.{ Failure, Try }

/**
 * @author Vincent76
 */
object PlaceRobberCommand extends GameSocketCommand( "placeRobber", SocketCommandScope.Turn ) {
  override def gameExecute( gameSession:GameSession, sessionID:String, data:String ):Try[JsValue] =
    data.toIntOption
      .map( hID => controllerAction( gameSession, _.action( _.placeRobber( hID ) ) ) )
      .getOrElse( Failure( InvalidInput( data ) ) )
}
