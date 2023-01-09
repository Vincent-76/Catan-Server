package model.socketcommands

import com.aimit.htwg.catan.CatanModule
import controllers.SessionController
import model.{ GameSession, SocketCommand }
import play.api.libs.json.{ JsValue, Json }

import scala.util.Try

/**
 * @author Vincent76
 */
object ModulesCommand extends SocketCommand( "modules" ) {

  override def executeRaw( sessionController:SessionController, sessionID:String, data:String ):Try[JsValue] = Try {
    Json.toJson( CatanModule.impls.map( _.name ) )
  }

}
