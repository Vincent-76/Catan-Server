package controllers

import akka.actor.ActorSystem
import com.aimit.htwg.catan.CatanModule
import com.aimit.htwg.catan.controller.Controller
import com.aimit.htwg.catan.model.{ FileIO, Game }
import com.aimit.htwg.catan.util.UndoManager
import com.google.inject.Guice
import model.GameSession
import play.api.inject.ApplicationLifecycle
import play.api.mvc.Session

import javax.inject._
import scala.collection.mutable
import scala.concurrent.duration.DurationInt
import scala.concurrent.{ ExecutionContext, Future }
import scala.reflect.io.File

/**
 * @author Vincent76
 */

@Singleton
class SessionController @Inject()( val actorSystem:ActorSystem, val lifeCycle: ApplicationLifecycle )( implicit executionContext:ExecutionContext ) {
  private val gameSessions:mutable.Map[String, GameSession] = mutable.Map()

  CatanModule.init()

  lifeCycle.addStopHook( () => saveAll() )
  private def saveAll( ):Future[_] = Future.sequence( gameSessions.toSeq.map( s => Future {
    s._2.controller.saveGame( Some( getSaveGamePath( s._1 ) ) )
  } ) )

  actorSystem.scheduler.scheduleAtFixedRate( 0.seconds, 1.minutes ) { () =>
    val now = java.time.Instant.now().getEpochSecond
    gameSessions.filterInPlace( ( sessionID, gameSession ) => {
      gameSession.controller.saveGame( Some( getSaveGamePath( sessionID ) ) )
      ( now - gameSession.timestamp ) < ( 60 * 60 )
    } )
  }

  private def getSaveGamePath( sessionID:String ):String =
    File( CatanModule.savegamePath ).createDirectory().path + File.separator + sessionID



  def hasGameSession( session:Session ):Boolean = session.get( "sessionID" ) match {
    case Some( sessionID ) => gameSessions.contains( sessionID ) || hasSaveGame( sessionID )
    case None => false
  }

  def deleteGameSession( session:Session ):Unit = session.get( "sessionID" ) match {
    case Some( sessionID ) =>
      gameSessions.remove( sessionID )
      deleteSaveGame( sessionID )
    case None =>
  }

  def newGameSession( session:Session, catanModule:CatanModule ):(Session, GameSession) = session.get( "sessionID" ) match {
    case Some( sessionID ) =>
      deleteSaveGame( sessionID )
      (session, createNewGameSession( sessionID, catanModule ))
    case None =>
      val sessionID = java.util.UUID.randomUUID().toString
      (session + ("sessionID" -> sessionID), createNewGameSession( sessionID, catanModule ))
  }

  def getGameSession( session:Session ):(Session, Option[GameSession]) = session.get( "sessionID" ) match {
    case Some( sessionID ) => (session, getGameSession( sessionID ))
    case None =>
      val sessionID = java.util.UUID.randomUUID().toString
      (session + ("sessionID" -> sessionID), getGameSession( sessionID ))
  }

  private def getGameSession( sessionID:String ):Option[GameSession] = gameSessions.get( sessionID ) match {
    case Some( gameSession ) =>
      gameSessions( sessionID ) = gameSession.update()
      Some( gameSession )
    case None => findSaveGamePath( sessionID ).map( path => {
      val loaded = FileIO.load( path )
      GameSession( new Controller( loaded._2._1, loaded._1, new UndoManager( loaded._2._2, loaded._2._3 ) ) )
    } );
  }

  private def createNewGameSession( sessionID:String, catanModule:CatanModule ):GameSession = {
    val injector = Guice.createInjector( catanModule )
    val gameSession = GameSession( new Controller( injector.getInstance( classOf[Game] ), injector.getInstance( classOf[FileIO] ) ) )
    gameSessions( sessionID ) = gameSession
    gameSession
  }

  private def hasSaveGame( sessionID:String ):Boolean = {
    val dir = new java.io.File( CatanModule.savegamePath )
    if( dir.exists && dir.isDirectory ) {
      try {
        dir.listFiles.exists( f => f.isFile && f.getName.matches( s"$sessionID\\..*" ) )
      } catch {
        case _:java.lang.SecurityException => false
      }
    } else false
  }

  private def findSaveGamePath( sessionID:String ):Option[String] = {
    val dir = new java.io.File( CatanModule.savegamePath )
    if( dir.exists && dir.isDirectory ) {
      try {
        dir.listFiles.find( f => f.isFile && f.getName.matches( s"$sessionID\\..*" ) ).map( _.getAbsolutePath )
      } catch {
        case _:java.lang.SecurityException => None
      }
    } else None
  }

  private def deleteSaveGame( sessionID:String ):Unit = {
    val dir = new java.io.File( CatanModule.savegamePath )
    if( dir.exists && dir.isDirectory ) {
      try {
        dir.listFiles.find( f => f.isFile && f.getName.matches( s"$sessionID\\..*" ) ).foreach( _.delete() )
      } catch {
        case _:java.lang.SecurityException => None
      }
    }
  }
}
