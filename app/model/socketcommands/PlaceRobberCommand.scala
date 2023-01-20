package model.socketcommands

import model.{ FormData, GameSession, GameSocketCommand, InputForm, InvalidInput, SocketCommandScope, TypedGameSocketCommand }
import play.api.libs.json.JsValue

import scala.util.{ Failure, Try }

/**
 * @author Vincent76
 */
object PlaceRobberCommand extends TypedGameSocketCommand( "placeRobber", InputForm.positiveIntForm, SocketCommandScope.Turn ) {
  override def typedGameExecute( gameSession:GameSession, sessionID:String, data:FormData[Int] ):Try[JsValue] =
    controllerAction( gameSession, _.action( _.placeRobber( data.value ) ) )
}
