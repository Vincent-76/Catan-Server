package model.socketcommands

import com.aimit.htwg.catan.model.FileIO
import controllers.SessionController
import model.{ GameSession, SocketCommand }
import play.api.libs.json.{ JsValue, Json }

import scala.util.Try

/**
 * @author Vincent76
 */
object FileIOsCommand extends SocketCommand( "fileIOs" ) {

  override def executeRaw( sessionController:SessionController, sessionID:String, data:String ):Try[JsValue] = Try {
    Json.toJson( FileIO.impls.map( _.name ) )
  }

}
