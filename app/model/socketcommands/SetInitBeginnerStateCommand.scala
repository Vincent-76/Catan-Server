package model.socketcommands

import model.{ GameSession, SocketCommand }
import play.api.libs.json.JsValue

import scala.util.Try

/**
 * @author Vincent76
 */
object SetInitBeginnerStateCommand extends SocketCommand( "setInitBeginnerState", onlyHost = true ) {
  override def exec( gameSession:GameSession, sessionID:String, data:String ):Try[JsValue] = {
    controllerAction( gameSession, _.action( _.setInitBeginnerState() ), broadcast = List( StateCommand ) )
  }
}
