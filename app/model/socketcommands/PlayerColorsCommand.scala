package model.socketcommands

import com.aimit.htwg.catan.model.PlayerColor
import controllers.SessionController
import model.{ GameSession, SocketCommand, SocketCommandScope }
import play.api.libs.json.{ JsValue, Json }

import scala.util.Try

/**
 * @author Vincent76
 */
object PlayerColorsCommand extends SocketCommand( "playerColors" ) {

  override def executeRaw( sessionController:SessionController, sessionID:String, data:String ):Try[JsValue] = Try {
    Json.toJson( PlayerColor.impls.map( _.name ) )
  }

}
