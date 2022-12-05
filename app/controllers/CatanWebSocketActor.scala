package controllers

import akka.actor.{ Actor, ActorRef, PoisonPill, Props }
import model.socketcommands._
import model.{ InvalidCommand, NoSession, SocketCommand }
import play.api.libs.json.{ JsValue, Json }

import scala.collection.mutable
import scala.util.{ Failure, Success, Try }

/**
 * @author Vincent76
 */
object CatanWebSocketActor {
  GameDataCommand.init()
  GameCommand.init()
  GameFieldCommand.init()
  PlayersCommand.init()
  ResourcesCommand.init()




  var actors:mutable.Map[String, List[CatanWebSocketActor]] = mutable.Map()


  def apply( out:ActorRef, sessionController:SessionController, sessionID:String ):CatanWebSocketActor =
    new CatanWebSocketActor( out, sessionController, sessionID )



  def addActor( actor:CatanWebSocketActor ):Unit = actors.get( actor.sessionID ) match {
    case Some( list ) => actors( actor.sessionID ) = list :+ actor
    case None => actors( actor.sessionID ) = List( actor )
  }

  def removeActor( actor:CatanWebSocketActor ):Unit =
    actors.get( actor.sessionID ).foreach( list => actors( actor.sessionID ) = list.filterNot( _ == actor ) )



  def broadcast( sessionID:String, cmd:String ):Unit = actors.get( sessionID ).foreach( _.foreach( actor =>
    actor.executeCommand( cmd )
  ) )
}


class CatanWebSocketActor( out:ActorRef, sessionController:SessionController, val sessionID:String ) extends Actor {

  override def preStart( ):Unit =
    CatanWebSocketActor.addActor( this )

  override def postStop():Unit = {
    CatanWebSocketActor.removeActor( this )
  }



  def send( cmd:String, msg:String ):Unit =
    out ! s"$cmd: $msg"

  def send( cmd:String, msg:JsValue ):Unit =
    send( cmd, Json.stringify( msg ) )

  def send( cmd:String, msg:Option[JsValue] ):Unit =
    send( cmd, msg.getOrElse( Json.obj() ) )

  def send( cmd:String, msg:Try[JsValue] ):Unit = msg match {
    case Success( value ) => send( cmd, value )
    case Failure( t ) => send( cmd, t.getMessage )
  }



  def receive:Receive = {
    case cmd:String =>
      executeCommand( cmd )
      println( s"Received [$cmd]" )
    case e => println( e )
  }

  private def executeCommand( cmd:String ):Unit = send( cmd, SocketCommand.of( cmd ) match {
    case Some( socketCommand ) =>
      sessionController.getGameSession( sessionID ) match {
        case Some( gameSession ) => socketCommand.execute( gameSession )
        case None => Failure( NoSession )
      }
    case None => Failure( InvalidCommand )
  } )



  def close( ):Unit =
    out ! PoisonPill
}
