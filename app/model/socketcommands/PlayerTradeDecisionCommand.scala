package model.socketcommands

import com.aimit.htwg.catan.model.Card.{ ResourceCards, resourceCardsReads }
import model._
import play.api.libs.json.JsValue

import scala.util.Try

/**
 * @author Vincent76
 */
object PlayerTradeDecisionCommand extends TypedGameSocketCommand( "playerTradeDecision", InputForm.boolForm, SocketCommandScope.Always ) {

  override def typedGameExecute( gameSession:GameSession, sessionID:String, decision:Boolean ):Try[JsValue] =
    controllerAction( gameSession, sessionID, _.action( _.playerTradeDecision( decision ) ) )
}
