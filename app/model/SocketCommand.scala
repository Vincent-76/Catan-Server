package model

import com.aimit.htwg.catan.controller.Controller
import com.aimit.htwg.catan.model.state.NextPlayerState
import com.aimit.htwg.catan.model._
import com.aimit.htwg.catan.util._
import com.aimit.htwg.catan.model.Card._
import controllers.{ CatanWebSocketActor, SessionController }
import play.api.data.Form
import play.api.libs.json.{ JsValue, Json }

import scala.util.{ Failure, Success, Try }

/**
 * @author Vincent76
 */

object SocketCommand extends NamedComponent[SocketCommand] {
  def getInfo( controller:Controller, info:Info ):Iterable[String] = info match {
    case info:DiceInfo => List( info.dices._1 + " + " + info.dices._2 + " = " + ( info.dices._1 + info.dices._2 ) )
    case GatherInfo( dices, playerResources ) =>
      List( dices._1 + " + " + dices._2 + " = " + ( dices._1 + dices._2 ) ) ++
        playerResources.map[String]( d => controller.player( d._1 ).name + " " + d._2.toString( "+" ) )
    case GotResourcesInfo( pID, cards ) =>
      List( controller.player( pID ).name + "  " + cards.toString( "+" ) )
    case LostResourcesInfo( pID, cards ) =>
      List( controller.player( pID ).name + "  " + cards.toString( "-" ) )
    case ResourceChangeInfo( playerAdd, playerSub ) =>
      val nameLength = ( playerAdd.keys ++ playerSub.keys ).map( controller.player( _ ).idName.length ).max
      playerSub.map( d => controller.player( d._1 ).name.toLength( nameLength ) + "  " + d._2.toString( "-" ) ) ++
        playerAdd.map( d => controller.player( d._1 ).name.toLength( nameLength ) + "  " + d._2.toString( "+" ) )
    case BankTradedInfo( _, give, get ) =>
      List( "You traded " + give.toString( "" ) + " for " + get.toString( "" ) + "." )
    case DrawnDevCardInfo( _, devCard ) =>
      List( "Drawn: " + devCard.name + "\n" + devCard.desc )
    case InsufficientStructuresInfo( _, structure ) =>
      List( "You don't have enough structures of " + structure.name + " to build more." )
    case NoPlacementPointsInfo( _, structure ) =>
      List( "There aren't any more possible placement points for structure " + structure.name + " to build more." )
    case GameEndInfo( winner ) =>
      val p = controller.player( winner )
      List( p.name + " won with " + controller.game.getPlayerVictoryPoints( p.id ) + " victory points!" )
    case GameSavedInfo( path ) => List( "Game was saved to: " + path )
    case GameLoadedInfo( path ) => List( "Game was loaded from: " + path )
    case _ => List()
  }
}


abstract class SocketCommand( name:String, val onlyHost:Boolean = false ) extends NamedComponentImpl( name ) {
  override def init():Unit = SocketCommand.addImpl( this )

  def executeRaw( sessionController:SessionController, sessionID:String, data:String ):Try[JsValue] =
    execute( sessionController, sessionID, sessionController.getGameSessionFromSessionID( sessionID ), data )

  def execute( sessionController:SessionController, sessionID:String, gameSession:Option[GameSession], data:String ):Try[JsValue] = gameSession match {
    case Some( session ) =>
      if( !onlyHost || session.hostID == sessionID )
        exec( session, sessionID, data )
      else Failure( Forbidden )
    case None => Failure( NoGame )
  }

  def exec( gameSession:GameSession, sessionID:String, data:String ):Try[JsValue] = Failure( NoAction )

  def controllerAction( gameSession:GameSession, f:Controller => Try[Option[Info]], broadcast:List[SocketCommand] = Nil, undo:Boolean = false ):Try[JsValue] = {
    val result = f( gameSession.controller )
    val res = if( gameSession.controller.game.state.isInstanceOf[NextPlayerState] ) {
      if( undo )
        gameSession.controller.undoAction()
      else {
        gameSession.controller.action( _.startTurn() )
        result
      }
    } else result
    res match {
      case Success( info ) =>
        broadcast.foreach( CatanWebSocketActor.broadcast( gameSession, _ ) )
        info.foreach( info => CatanWebSocketActor.broadcastInfo( gameSession, SocketCommand.getInfo( gameSession.controller, info ).mkString( "\n" ) ) )
        Success( Json.obj() )
      case Failure( t ) => Failure( SocketError.fromControllerError( gameSession.controller, t ) )
    }
  }
}

abstract class TypedSocketCommand[T]( name:String, form:Form[T], onlyHost:Boolean = false ) extends SocketCommand( name, onlyHost ) {
  override def execute( sessionController:SessionController, sessionID:String, gameSession:Option[GameSession], data:String ):Try[JsValue] = Try {
    val boundForm:Form[T] = form.bind( Json.parse( data ), Form.FromJsonMaxChars )
    executeTyped( sessionController, sessionID, gameSession, boundForm.get )
  }.flatten

  def executeTyped( sessionController:SessionController, sessionID:String, gameSession:Option[GameSession], data:T ):Try[JsValue] = gameSession match {
    case Some( session ) =>
      if( !onlyHost || session.hostID == sessionID )
        execTyped( session, sessionID, data )
      else Failure( Forbidden )
    case None => Failure( NoGame )
  }

  def execTyped( gameSession:GameSession, sessionID:String, data:T ):Try[JsValue] = Failure( NoAction )
}