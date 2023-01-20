package model.socketcommands

import com.aimit.htwg.catan.model.Card.{ ResourceCards, resourceCardsReads }
import com.aimit.htwg.catan.model.Resource
import model._
import play.api.libs.json.JsValue

import scala.util.Try

/**
 * @author Vincent76
 */
object MonopolyActionCommand extends TypedGameSocketCommand( "monopolyAction", InputForm.componentForm( Resource ), SocketCommandScope.Turn ) {

  override def typedGameExecute( gameSession:GameSession, sessionID:String, resource:Resource ):Try[JsValue] =
    controllerAction( gameSession, _.action( _.monopolyAction( resource ) ) )
}
