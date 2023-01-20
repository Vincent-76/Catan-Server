package model.socketcommands

import com.aimit.htwg.catan.model.Card.{ ResourceCards, resourceCardsReads }
import model._
import play.api.libs.json.JsValue

import scala.util.Try

/**
 * @author Vincent76
 */
object YearOfPlentyActionCommand extends TypedGameSocketCommand( "yearOfPlentyAction", InputForm.jsonForm[ResourceCards], SocketCommandScope.Turn ) {

  override def typedGameExecute( gameSession:GameSession, sessionID:String, data:FormData[ResourceCards] ):Try[JsValue] =
    controllerAction( gameSession, _.action( _.yearOfPlentyAction( data.value ) ) )
}
