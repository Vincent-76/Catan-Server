package model.socketcommands

import com.aimit.htwg.catan.model.PlayerID
import model._
import play.api.libs.json.JsValue

import scala.util.Try

/**
 * @author Vincent76
 */
object PlayerTradeCommand extends TypedGameSocketCommand( "playerTrade", InputForm.jsonForm[PlayerID], SocketCommandScope.Turn ) {

  override def typedGameExecute( gameSession:GameSession, sessionID:String, data:FormData[PlayerID] ):Try[JsValue] =
    controllerAction( gameSession, _.action( _.playerTrade( data.value ) ) )
}
