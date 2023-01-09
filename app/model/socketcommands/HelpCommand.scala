package model.socketcommands

import controllers.SessionController
import model.{ GameSession, SocketCommand }
import play.api.libs.json.{ JsValue, Json }

import scala.util.{ Success, Try }

/**
 * @author Vincent76
 */
object HelpCommand extends SocketCommand( "help" ) {

  override def executeRaw( sessionController:SessionController, sessionID:String, data:String ):Try[JsValue] = Success(
    Json.toJson( SocketCommand.impls.map( _.name ) )
  )
}
