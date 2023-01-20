package model.socketcommands

import com.aimit.htwg.catan.model.DevelopmentCard
import model.{ GameSession, InputForm, SocketCommandScope, TypedGameSocketCommand }
import play.api.libs.json.JsValue

import scala.util.Try

/**
 * @author Vincent76
 */
object UseDevCardCommand extends TypedGameSocketCommand( "useDevCard", InputForm.componentForm( DevelopmentCard ), SocketCommandScope.Turn ) {
  override def typedGameExecute( gameSession:GameSession, sessionID:String, devCard:DevelopmentCard ):Try[JsValue] =
    controllerAction( gameSession, sessionID, _.action( _.useDevCard( devCard ) ) )
}
