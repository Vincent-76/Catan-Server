package model.socketcommands

import com.aimit.htwg.catan.model.Card.{ ResourceCards, resourceCardsReads }
import model._
import play.api.libs.json.JsValue

import scala.util.Try

/**
 * @author Vincent76
 */
object SetPlayerTradeStateCommand extends
  TypedGameSocketCommand( "setPlayerTradeState",
    InputForm.doubleForm( "give" -> InputForm.jsonMapping[ResourceCards], "get" -> InputForm.jsonMapping[ResourceCards] ),
    SocketCommandScope.Turn
  ) {

  override def typedGameExecute( gameSession:GameSession, sessionID:String, data:(ResourceCards, ResourceCards) ):Try[JsValue] =
    controllerAction( gameSession, sessionID, _.action( _.setPlayerTradeState( data._1, data._2 ) ) )
}
