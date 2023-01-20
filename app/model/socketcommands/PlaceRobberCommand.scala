package model.socketcommands

import model.{ GameSession, InputForm, SocketCommandScope, TypedGameSocketCommand }
import play.api.libs.json.JsValue

import scala.util.Try

/**
 * @author Vincent76
 */
object PlaceRobberCommand extends TypedGameSocketCommand( "placeRobber", InputForm.positiveIntForm, SocketCommandScope.Turn ) {
  override def typedGameExecute( gameSession:GameSession, sessionID:String, id:Int ):Try[JsValue] =
    controllerAction( gameSession, _.action( _.placeRobber( id ) ) )
}
