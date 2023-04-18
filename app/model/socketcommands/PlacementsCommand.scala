package model.socketcommands

import com.aimit.htwg.catan.CatanModule
import com.aimit.htwg.catan.model.Placement
import controllers.SessionController
import model.{ GameSession, SocketCommand, SocketCommandScope }
import play.api.libs.json.{ JsValue, Json }

import scala.util.Try

/**
 * @author Vincent76
 */
object PlacementsCommand extends SocketCommand( "placements" ) {

  override def executeRaw( sessionController:SessionController, sessionID:String, data:String ):Try[JsValue] = Try {
    Json.toJson( Placement.impls.map( _.name ) )
  }

}
