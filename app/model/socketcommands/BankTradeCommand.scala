package model.socketcommands

import com.aimit.htwg.catan.model.Card.{ ResourceCards, resourceCardsReads }
import model.InputForm.TestClass
import model._
import play.api.libs.json.JsValue

import scala.util.Try

/**
 * @author Vincent76
 */
object BankTradeCommand extends
  TypedGameSocketCommand( "bankTrade",
    InputForm.resourceCardsForm( "give" -> InputForm.jsonMapping[ResourceCards], "get" -> InputForm.jsonMapping[ResourceCards] ),
    SocketCommandScope.Always //SocketCommandScope.Turn TODO
  ) {

  override def typedGameExecute( gameSession:GameSession, sessionID:String, data:TestClass ):Try[JsValue] =
    controllerAction( gameSession, sessionID, _.action( _.bankTrade( data.give, data.get ) ) )
}
