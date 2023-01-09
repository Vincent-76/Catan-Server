package controllers

import akka.actor.{ Actor, ActorRef, PoisonPill }
import com.aimit.htwg.catan.util.RichAny
import model.socketcommands._
import model.{ GameSession, InvalidCommand, NoSessionFound, SocketCommand, SocketError }
import play.api.libs.json.{ JsValue, Json }

import scala.collection.mutable
import scala.util.{ Failure, Success, Try }

/**
 * @author Vincent76
 */
object CatanWebSocketActor {
  HelpCommand.init()
  GameDataCommand.init()
  ModulesCommand.init()
  PlacementsCommand.init()
  FileIOsCommand.init()
  PlayerColorsCommand.init()
  NewGameCommand.init()
  CreateGameCommand.init()
  JoinGameCommand.init()

  GameCommand.init()
  GameFieldCommand.init()
  PlayersCommand.init()
  StateCommand.init()
  ResourcesCommand.init()
  TurnCommand.init()

  AddPlayerCommand.init()
  SetInitBeginnerStateCommand.init()
  DiceOutBeginnerCommand.init()
  SetBeginnerCommand.init()





  var actors:mutable.Map[String, CatanWebSocketActor] = mutable.Map()


  def apply( out:ActorRef, sessionController:SessionController, sessionID:String ):CatanWebSocketActor =
    new CatanWebSocketActor( out, sessionController, sessionID )



  def addActor( actor:CatanWebSocketActor ):Unit = actors( actor.sessionID ) = actor

  def removeActor( actor:CatanWebSocketActor ):Unit = actors.remove( actor.sessionID )


  private def gameSessionActors( gameSession:GameSession, source:Option[String] = None, f:CatanWebSocketActor => Unit ):Unit =
    gameSession.players.keySet.filterNot( source.contains( _ ) ).map( actors.get( _ ) ).foreach( _.foreach( f ) )

  def broadcast( gameSession:GameSession, cmd:SocketCommand, data:String = "", source:Option[String] = None ):Unit =
    gameSessionActors( gameSession, source, _.performCommand( s"?$cmd", cmd, data ) )

  def broadcastInfo( gameSession:GameSession, info:String, source:Option[String] = None ):Unit =
    gameSessionActors( gameSession, source, _.send( "?info", info ) )

}


class CatanWebSocketActor( out:ActorRef, sessionController:SessionController, val sessionID:String ) extends Actor {

  override def preStart( ):Unit =
    CatanWebSocketActor.addActor( this )

  override def postStop():Unit = {
    CatanWebSocketActor.removeActor( this )
  }



  def send( id:String, msg:String ):Unit =
    out ! s"$id|$msg"

  def send( id:String, msg:JsValue ):Unit =
    send( id, Json.stringify( msg ) )

  def send( id:String, msg:Option[JsValue] ):Unit =
    send( id, msg.getOrElse( Json.obj() ) )

  def send( id:String, msg:Try[JsValue] ):Unit = msg match {
    case Success( value ) => send( id, value )
    case Failure( t:SocketError ) => send( s"!$id", t.code + "|" + t.getMessage )
    case Failure( t ) => send( s"!$id", t.getMessage )
  }



  def receive:Receive = {
    case data:String =>
      data.indexOf( "|" ) match {
        case -1 => send( "?", Failure( InvalidCommand ) )
        case i => data.indexOf( "|", i + 1 ) match {
          case -1 => executeCommand( data.substring( 0, i ), data.substring( i + 1 ), "" )
          case j => executeCommand( data.substring( 0, i ), data.substring( i + 1, j ), data.substring( j + 1 ) )
        }
      }
    case e => println( e )
  }

  private def executeCommand( id:String, cmd:String, data:String ):Unit = SocketCommand.of( cmd ) match {
    case Some( socketCommand ) => performCommand( id, socketCommand, data )
    case None => send( id, Failure( InvalidCommand ) )
  }

  def performCommand( id:String, cmd:SocketCommand, data:String ):Unit =
    send( id, cmd.executeRaw( sessionController, sessionID, data ) )



  def close( ):Unit =
    out ! PoisonPill
}
