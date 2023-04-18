package model.socketcommands

import com.aimit.htwg.catan.model.PlayerID
import model._
import play.api.libs.json.JsValue

import scala.util.Try

/**
 * @author Vincent76
 */
object RobberStealCommand extends TypedGameSocketCommand( "robberSteal", InputForm.jsonForm[PlayerID], SocketCommandScope.Turn ) {

  override def typedGameExecute( gameSession:GameSession, sessionID:String, pID:PlayerID ):Try[JsValue] =
    controllerAction( gameSession, sessionID, _.action( _.robberStealFromPlayer( pID ) ) )
}
