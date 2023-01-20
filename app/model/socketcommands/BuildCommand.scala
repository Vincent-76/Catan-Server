package model.socketcommands

import model.{ FormData, GameSession, GameSocketCommand, InputForm, InvalidInput, SocketCommandScope, TypedGameSocketCommand }
import play.api.libs.json.JsValue

import scala.util.{ Failure, Try }

/**
 * @author Vincent76
 */
object BuildCommand extends TypedGameSocketCommand( "build", InputForm.positiveIntForm, SocketCommandScope.Turn ) {
  override def typedGameExecute( gameSession:GameSession, sessionID:String, data:FormData[Int] ):Try[JsValue] =
    controllerAction( gameSession, _.action( _.build( data.value ) ) )
}
