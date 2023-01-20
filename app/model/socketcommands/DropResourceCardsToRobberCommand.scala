package model.socketcommands

import com.aimit.htwg.catan.model.Card.{ ResourceCards, resourceCardsReads }
import model._
import play.api.libs.json.JsValue

import scala.util.Try

/**
 * @author Vincent76
 */
object DropResourceCardsToRobberCommand extends TypedGameSocketCommand( "dropResourceCardsToRobber", InputForm.jsonForm[ResourceCards], SocketCommandScope.Always ) {

  override def typedGameExecute( gameSession:GameSession, sessionID:String, data:FormData[ResourceCards] ):Try[JsValue] =
    controllerAction( gameSession, _.action( _.dropResourceCardsToRobber( data.value ) ) )
}
