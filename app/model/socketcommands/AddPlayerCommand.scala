package model.socketcommands

import com.aimit.htwg.catan.util.RichAny
import controllers.{ CatanWebSocketActor, SessionController }
import model.form.AddPlayer
import model.{ AlreadyRegistered, GameSession, NoGame, SocketCommandScope, TypedGameSocketCommand, TypedSocketCommand }
import play.api.libs.json.{ JsValue, Json }

import scala.util.{ Failure, Success, Try }

/**
 * @author Vincent76
 */

object AddPlayerCommand extends TypedSocketCommand[AddPlayer]( "addPlayer", AddPlayer.form ) {

  override def typedExecute( sessionController:SessionController, sessionID:String, gameSession:Option[GameSession], data:AddPlayer ):Try[JsValue] = gameSession match {
    case Some( gameSession ) =>
      if( gameSession.players.get( sessionID ).use( o => o.isDefined && o.get.isEmpty ) ) {
        controllerAction( gameSession, sessionID, _.action( _.addPlayer( data.color, data.name ) ), update = false )
        sessionController.setPlayerID( gameSession, sessionID )
        CatanWebSocketActor.broadcast( gameSession, GameDataCommand )
        Success( Json.obj() )
      } else Failure( AlreadyRegistered )
    case None => Failure( NoGame )
  }
}
