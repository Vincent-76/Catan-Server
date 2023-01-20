package controllers

import akka.actor.{ Actor, ActorRef, PoisonPill }
import com.aimit.htwg.catan.util.RichString
import model.socketcommands._
import model.{ GameSession, InvalidCommand, NoSessionFound, SocketCommand, SocketError }
import play.api.libs.json.{ JsArray, JsValue, Json }

import java.time.Instant
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
  GameValuesCommand.init()
  GameStatusCommand.init()
  GameFieldCommand.init()
  PlayersCommand.init()
  StateCommand.init()
  ResourcesCommand.init()
  TurnCommand.init()

  AddPlayerCommand.init()
  SetInitBeginnerStateCommand.init()
  DiceOutBeginnerCommand.init()
  SetBeginnerCommand.init()
  PlaceRobberCommand.init()
  UseDevCardCommand.init()
  BuildCommand.init()
  AbortPlayerTradeCommand.init()
  BankTradeCommand.init()
  BuyDevCardCommand.init()
  DropResourceCardsToRobberCommand.init()
  EndTurnCommand.init()
  MonopolyActionCommand.init()
  PlayerTradeCommand.init()
  PlayerTradeDecisionCommand.init()
  RobberStealFromPlayerCommand.init()
  SetBuildStateCommand.init()
  SetPlayerTradeStateCommand.init()
  YearOfPlentyActionCommand.init()





  var actors:mutable.Map[String, ActorHolder] = mutable.Map()


  def apply( out:ActorRef, sessionController:SessionController, sessionID:String ):CatanWebSocketActor =
    new CatanWebSocketActor( out, sessionController, sessionID )



  def log( event:String, msg:String ):Unit = s"${s"$event:".toLength( 20 )} $msg" match {
    case s:String if s.length > 150 => println( s.substring( 0, 150 ) + " ..." )
    case s:String => println( s )
  }


  def addActor( actor:CatanWebSocketActor ):Unit = actors.get( actor.sessionID ) match {
    case Some( actorHolder ) => actorHolder.setActor( actor )
    case None => actors( actor.sessionID ) = new ActorHolder( actor.sessionID, actor.sessionController, Some( actor ) )
  }

  def removeActor( actor:CatanWebSocketActor ):Unit =
    actors.get( actor.sessionID ).foreach( _.unsetActor() )


  def actorAction( sessionID:String, action:ActorHolder => Unit, actor:Option[CatanWebSocketActor] = None ):Unit = actors.get( sessionID ) match {
    case Some( actorHolder ) => action( actorHolder )
    case None => if( actor.isDefined ) {
      val actorHolder = new ActorHolder( actor.get.sessionID, actor.get.sessionController, actor )
      actors( sessionID ) = actorHolder
      action( actorHolder )
    }
  }


  private def gameSessionActors( gameSession:GameSession, source:Option[String] = None, f:ActorHolder => Unit ):Unit =
    gameSession.players.keySet.filterNot( source.contains( _ ) ).map( actors.get( _ ) ).foreach( _.foreach( f ) )

  def broadcast( gameSession:GameSession, cmd:SocketCommand, data:String = "", source:Option[String] = None ):Unit =
    gameSessionActors( gameSession, source, _.performCommand( s"?$cmd", cmd, data ) )

  def broadcastInfo( gameSession:GameSession, info:String, source:Option[String] = None ):Unit =
    broadcastEvent( gameSession, "info", info, source )

  def broadcastEvent( gameSession:GameSession, event:String, data:String, source:Option[String] = None ):Unit = {
    gameSessionActors( gameSession, source, _.event( event, data ) )
  }

}


class ActorHolder( val sessionID:String,
                   sessionController:SessionController,
                   var actor:Option[CatanWebSocketActor],
                   var queue:mutable.SortedMap[Int, String] = mutable.TreeMap.empty,
                   var nextID:Int = 1,
                   var lastUsed:Long = Instant.now().getEpochSecond
                 ) {

  private def updateLastUsed():Unit = {
    this.lastUsed = Instant.now().getEpochSecond
  }

  def setActor( actor:CatanWebSocketActor ):Unit = {
    this.actor = Some( actor )
    updateLastUsed()
    if( queue.nonEmpty )
      actor.send( "[" + queue.map( e => s"\"${e._1}|${e._2}\"" ).mkString( "," ) + "]" )
  }

  def unsetActor():Unit =
    this.actor = None


  def received( id:Int ):Unit = {
    CatanWebSocketActor.log( "Received", id.toString )
    queue.remove( id )
    //copy( queue = queue.removed( id ), lastUsed = Instant.now().getEpochSecond  )
  }


  def send( msg:String ):Unit = this.synchronized {
    val sendMsg = s"$nextID|$msg"
    //val newQueue = queue + ( nextID -> sendMsg )
    queue( nextID ) = sendMsg
    nextID += 1
    actor.foreach( _.send( sendMsg ) )
    //copy( queue = newQueue, nextID = nextID + 1 )
  }

  def event( event:String, data:String ):Unit =
    send( s"?$event|$data" )

  def reply( id:String, data:String ):Unit =
    send( s"$id|$data" )

  def replyError( id:String, t:Throwable ):Unit = {
    t.printStackTrace()
    send( s"!$id|" + ( t match {
      case t:SocketError => s"${t.code}|${t.getMessage}"
      case _ => t.getMessage
    } ) )
  }

  def reply( id:String, data:JsValue ):Unit =
    reply( id, Json.stringify( data ) )

  def reply( id:String, data:Option[JsValue] ):Unit =
    reply( id, data.getOrElse( Json.obj() ) )

  def reply( id:String, data:Try[JsValue] ):Unit = data match {
    case Success( value ) => reply( id, value )
    case Failure( t ) => replyError( id, t )
  }


  def sendLastID():Unit = this.synchronized {
    actor.foreach( _.send( s"-${if( queue.nonEmpty ) queue.keys.min else nextID}" ) )
    //this
  }


  def performCommand( id:String, cmd:SocketCommand, data:String ):Unit =
    reply( id, cmd.executeRaw( sessionController, sessionID, data ) )
}


class CatanWebSocketActor( out:ActorRef, val sessionController:SessionController, val sessionID:String ) extends Actor {

  override def preStart( ):Unit =
    CatanWebSocketActor.addActor( this )

  override def postStop():Unit = {
    CatanWebSocketActor.removeActor( this )
  }

  def send( msg:String ):Unit = {
    out ! msg
    CatanWebSocketActor.log( "Sent", msg )
  }


  def receive:Receive = {
    case dataString:String =>
      CatanWebSocketActor.log( "Msg", dataString )
      if( dataString.startsWith( "[" ) ) Json.parse( dataString ) match {
        case dataList:JsArray => dataList.value.foreach( _.asOpt[String] match {
          case Some( data ) => handleData( data )
          case None => invalidInput( dataString )
        } )
        case _ => invalidInput( dataString )
      } else handleData( dataString )

    case e => println( e )
  }

  private def invalidInput( data:String ):Unit = {
    CatanWebSocketActor.log( "InvalidCommand", data )
    CatanWebSocketActor.actorAction( sessionID, _.replyError( "", InvalidCommand ) )
  }

  private def handleData( data:String ):Unit = {
    if( data == "-" )
      CatanWebSocketActor.actorAction( sessionID, _.sendLastID(), Some( this ) )
    else if( data.startsWith( "!" ) ) data.substring( 1 ).toIntOption match {
      case Some( id ) => CatanWebSocketActor.actorAction( sessionID, _.received( id ), Some( this ) )
      case None => invalidInput( data )
    } else {
      data.indexOf( "|" ) match {
        case -1 => invalidInput( data )
        case i => data.indexOf( "|", i + 1 ) match {
          case -1 => executeCommand( data.substring( 0, i ), data.substring( i + 1 ), "" )
          case j => executeCommand( data.substring( 0, i ), data.substring( i + 1, j ), data.substring( j + 1 ) )
        }
      }
    }
  }

  private def executeCommand( id:String, cmd:String, data:String ):Unit = SocketCommand.of( cmd ) match {
    case Some( socketCommand ) => CatanWebSocketActor.actorAction( sessionID, _.performCommand( id, socketCommand, data ), Some( this ) )
    case None => invalidInput( cmd )
  }

  def close( ):Unit =
    out ! PoisonPill
}
