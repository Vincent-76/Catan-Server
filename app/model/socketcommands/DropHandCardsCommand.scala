package model.socketcommands

import com.aimit.htwg.catan.model.Card.{ ResourceCards, resourceCardsReads }
import model._
import play.api.libs.json.JsValue

import scala.util.Try

/**
 * @author Vincent76
 */
object DropHandCardsCommand extends TypedGameSocketCommand( "dropHandCards", InputForm.jsonForm[ResourceCards], SocketCommandScope.Always ) {

  override def typedGameExecute( gameSession:GameSession, sessionID:String, data:ResourceCards ):Try[JsValue] =
    controllerAction( gameSession, _.action( _.dropResourceCardsToRobber( data ) ) )
}
