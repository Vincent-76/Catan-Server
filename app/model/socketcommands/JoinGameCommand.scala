package model.socketcommands

import com.aimit.htwg.catan.model.state.InitPlayerState
import controllers.SessionController
import model.{ GameAlreadyFull, GameData, NoGame, NotPossibleAnymore, SocketCommand, SocketCommandScope }
import play.api.libs.json.JsValue

import scala.util.{ Failure, Success, Try }

/**
 * @author Vincent76
 */

object JoinGameCommand extends SocketCommand( "joinGame" ) {

  override def executeRaw( sessionController:SessionController, sessionID:String, data:String ):Try[JsValue] =
    sessionController.getGameSessionFromGameID( data ) match {
      case Some( gameSession ) =>
        if( !gameSession.controller.game.state.isInstanceOf[InitPlayerState] )
          Failure( NotPossibleAnymore )
        else if( gameSession.players.size >= gameSession.controller.game.maxPlayers )
          Failure( GameAlreadyFull )
        else {
          sessionController.deleteGameSession( sessionID )
          sessionController.addPlayer( gameSession, sessionID )
          Success( GameData( sessionID, gameSession ).toJson )
        }
      case None => Failure( NoGame )
    }

}
