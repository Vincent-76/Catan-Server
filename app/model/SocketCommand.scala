package model

import com.aimit.htwg.catan.controller.Controller
import com.aimit.htwg.catan.model.state.NextPlayerState
import com.aimit.htwg.catan.model.impl.fileio.JsonFileIO.JsonMap
import com.aimit.htwg.catan.model._
import com.aimit.htwg.catan.util._
import com.aimit.htwg.catan.model.Card._
import controllers.{ CatanWebSocketActor, SessionController }
import model.SocketCommand.updateChanges
import model.socketcommands.{ GameCommand, GameDataCommand, GameFieldCommand, GameStatusCommand, PlayersCommand, ResourcesCommand, StateCommand, TurnCommand }
import play.api.data.Form
import play.api.libs.json.{ JsArray, JsBoolean, JsNull, JsNumber, JsObject, JsString, JsValue, Json }

import scala.util.{ Failure, Success, Try }

/**
 * @author Vincent76
 */

object SocketCommand extends NamedComponent[SocketCommand] {
  def bindData[T, R]( form:Form[T], data:String, f:T => R ):Try[R] = Try {
    f( form.bind( Json.parse( data ), Form.FromJsonMaxChars ).get )
  }

  def updateChanges( gameSession:GameSession, o:Game, n:Game ):Unit = List(
    (o.gameField, n.gameField, GameFieldCommand),
    (o.turn, n.turn, TurnCommand),
    (o.state, n.state, StateCommand),
    (o.resourceCards, n.resourceCards, ResourcesCommand),
    (o.devCards, n.devCards, GameCommand),
    (o.players, n.players, PlayersCommand),
    (o.bonusCards, n.bonusCards, GameCommand),
    (o.winner, n.winner, GameCommand),
  ).filter( e => e._1 != e._2 ).map( _._3 ).distinct.foreach( CatanWebSocketActor.broadcast( gameSession, _ ) )

  def jsonDiff( oldJson:JsValue, newJson:JsValue, path:List[JsValue] = Nil ):Map[List[JsValue], JsValue] = {
    //if( path.contains( Json.toJson( "vertices" ) ) && newJson.isInstanceOf[JsObject] && newJson.asInstanceOf[JsObject].value.get( "id" ).contains( Json.toJson( 1 ) ) ) {
    //  println( path.map( Json.stringify ).mkString( "." ) + ": Old[" + Json.stringify( oldJson ) + "], New[" + Json.stringify( newJson ) + "]" )
    //}
    (oldJson, newJson) match {
      case (o:JsArray, n:JsArray) =>
        if( n.value.nonEmpty ) {
          n.value.head match {
            case v:JsObject if v.value.contains( "k" ) && v.value.contains( "v" ) =>
              val oMapped = o.value.map( _.asInstanceOf[JsObject] )
              val oKeys = oMapped.map( e => e.value( "k" ) ).toSet
              val nKeys = n.value.map( e => e.asInstanceOf[JsObject].value( "k" ) ).toSet
              if( oKeys.subsetOf( nKeys ) ) {
                n.value.map( _.asInstanceOf[JsObject] ).foldLeft( Map.empty[List[JsValue], JsValue] )( ( m, e ) => {
                  m ++ jsonDiff( oMapped.find( _.value( "k" ) == e.value( "k" ) ).map( _.value( "v" ) ).getOrElse( JsNull ), e.value( "v" ), path :+ Json.obj( "k" -> e.value( "k" ) ) )
                } )
              } else Map( path -> n )
            case _ =>
              if( o.value.size == n.value.size )
                o.value.zipWithIndex.foldLeft( Map.empty[List[JsValue], JsValue] )( ( m, e ) =>
                  m ++ jsonDiff( e._1, n.value( e._2 ), path :+ Json.toJson( e._2 ) )
                )
              else Map( path -> n )
          }
        } else if( o.value.nonEmpty ) Map( path -> n )
        else Map.empty
      case (o:JsObject, n:JsObject) if o.value.keySet.subsetOf( n.value.keySet ) =>
        n.value.foldLeft( Map.empty[List[JsValue], JsValue] )( ( m, e ) =>
          m ++ o.value.get( e._1 ).map( jsonDiff( _, e._2, path :+ Json.toJson( e._1 ) ) )
            .getOrElse( Map( ( path :+ Json.toJson( e._1 ) ) -> e._2 ) )
        )
      case (o:JsBoolean, n:JsBoolean) if o.value == n.value =>
        Map.empty
      case (o:JsNumber, n:JsNumber) if o.value == n.value =>
        Map.empty
      case (o:JsString, n:JsString) if o.value == n.value =>
        Map.empty
      case (JsNull, JsNull) =>
        Map.empty
      case _ =>
        Map( path -> newJson )
    }
  }

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



sealed trait SocketCommandScope
object SocketCommandScope {
  case object Always extends SocketCommandScope
  case object Host extends SocketCommandScope
  case object Turn extends SocketCommandScope
}

abstract class SocketCommand( name:String ) extends NamedComponentImpl( name ) {
  override def init():Unit = SocketCommand.addImpl( this )

  def executeRaw( sessionController:SessionController, sessionID:String, data:String ):Try[JsValue] =
    execute( sessionController, sessionID, sessionController.getGameSessionFromSessionID( sessionID ), data )

  def execute( sessionController:SessionController, sessionID:String, gameSession:Option[GameSession], data:String ):Try[JsValue] = Failure( NoAction )

  def checkBroadcast[T]( gameSession:GameSession, o1:T, o2:T, socketCommand:SocketCommand ):Unit =
    if( o1 != o2 ) CatanWebSocketActor.broadcast( gameSession, socketCommand )

  def controllerAction( gameSession:GameSession, f:Controller => Try[Option[Info]], broadcast:List[SocketCommand] = Nil, undo:Boolean = false, update:Boolean = true ):Try[JsValue] = {
    val r = gameSession.controller.synchronized {
      val oldGame = gameSession.controller.game
      val result = f( gameSession.controller )
      val res = if( gameSession.controller.game.state.isInstanceOf[NextPlayerState] ) {
        if( undo )
          gameSession.controller.undoAction()
        else {
          gameSession.controller.action( _.startTurn() )
          result
        }
      } else result
      (res, oldGame, gameSession.controller.game )
    }
    r._1 match {
      case Success( info ) =>
        if( update && !broadcast.contains( GameDataCommand ) ) {
          //updateChanges( gameSession, oldGame, gameSession.controller.game )
          val oldJson = Json.toJson( r._2 )
          val newJson = Json.toJson( r._3 )
          //println( "OldJson: \n" + Json.stringify( oldJson ) )
          //println( "NewJson: \n" + Json.stringify( newJson ) )
          val difference = SocketCommand.jsonDiff( oldJson, newJson )
          if( difference.nonEmpty )
            CatanWebSocketActor.broadcastEvent( gameSession, "gameUpdate", Json.stringify( difference.toJson ) )
          CatanWebSocketActor.broadcast( gameSession, GameStatusCommand )
        }
        broadcast.foreach( CatanWebSocketActor.broadcast( gameSession, _ ) )
        info.foreach( info => CatanWebSocketActor.broadcastInfo( gameSession, SocketCommand.getInfo( gameSession.controller, info ).mkString( "\n" ) ) )
        Success( Json.obj() )
      case Failure( t ) => Failure( SocketError.fromControllerError( gameSession.controller, t ) )
    }
  }
}

abstract class GameSocketCommand( name:String, scope:SocketCommandScope ) extends SocketCommand( name ) {
  private def scopeValid( gameSession:GameSession, sessionID:String ):Boolean = scope match {
    case SocketCommandScope.Always => true
    case SocketCommandScope.Host => gameSession.hostID == sessionID
    case SocketCommandScope.Turn => gameSession.players.get( sessionID ).flatten.contains( gameSession.controller.onTurn )
  }

  override def execute( sessionController:SessionController, sessionID:String, gameSession:Option[GameSession], data:String ):Try[JsValue] = gameSession match {
    case Some( session ) =>
      if( scopeValid( session, sessionID ) )
        gameExecute( session, sessionID, data )
      else Failure( Forbidden )
    case None => Failure( NoGame )
  }

  def gameExecute( gameSession:GameSession, sessionID:String, data:String ):Try[JsValue] = Failure( NoAction )
}

abstract class TypedSocketCommand[T]( name:String, form:Form[T] ) extends SocketCommand( name ) {
  override def execute( sessionController:SessionController, sessionID:String, gameSession:Option[GameSession], data:String ):Try[JsValue] =
    SocketCommand.bindData( form, data, data => typedExecute( sessionController, sessionID, gameSession, data ) ).flatten

  def typedExecute( sessionController:SessionController, sessionID:String, gameSession:Option[GameSession], data:T ):Try[JsValue]
}

abstract class TypedGameSocketCommand[T]( name:String, form:Form[T], scope:SocketCommandScope ) extends GameSocketCommand( name, scope ) {
  override def gameExecute( gameSession:GameSession, sessionID:String, data:String ):Try[JsValue] =
    SocketCommand.bindData( form, data, data => typedGameExecute( gameSession, sessionID, data ) ).flatten

  def typedGameExecute( gameSession:GameSession, sessionID:String, data:T ):Try[JsValue]
}