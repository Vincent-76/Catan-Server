package model.socketcommands

import com.aimit.htwg.catan.model.DevelopmentCard
import model.{ GameSession, GameSocketCommand, InvalidInput, SocketCommandScope }
import play.api.libs.json.JsValue

import scala.util.{ Failure, Try }

/**
 * @author Vincent76
 */
object UseDevCardCommand extends GameSocketCommand( "useDevCard", SocketCommandScope.Turn ) {
  override def gameExecute( gameSession:GameSession, sessionID:String, data:String ):Try[JsValue] =
    DevelopmentCard.of( data )
      .map( devCard => controllerAction( gameSession, _.action( _.useDevCard( devCard ) ) ) )
      .getOrElse( Failure( InvalidInput( data ) ) )
}
