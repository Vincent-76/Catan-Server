package controllers

import akka.actor.ActorSystem
import com.aimit.htwg.catan.CatanModule
import com.aimit.htwg.catan.controller.Controller
import com.google.inject.{ Guice, Injector }
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
  val _injector:Injector = Guice.createInjector( new CatanModule( test = false ) )
  val _gameSessions:mutable.Map[String, GameSession] = mutable.Map()

  CatanModule.init()

  lifeCycle.addStopHook( () => saveAll() )
  private def saveAll( ):Future[_] = Future.sequence( _gameSessions.toSeq.map( s => Future {
    s._2.controller.saveGame( Some( getSaveGamePath( s._1 ) ) )
  } ) )

  actorSystem.scheduler.scheduleAtFixedRate( 0.seconds, 1.minutes ) { () =>
    val now = java.time.Instant.now().getEpochSecond
    _gameSessions.filterInPlace( ( sessionID, gameSession ) => {
      gameSession.controller.saveGame( Some( getSaveGamePath( sessionID ) ) )
      ( now - gameSession.timestamp ) < ( 60 * 60 )
    } )
  }

  private def getSaveGamePath( sessionID:String ):String =
    File( CatanModule.savegamePath ).createDirectory().path + File.separator + sessionID



  def getGameSession( session:Session ):(Session, GameSession) = session.get( "sessionID" ) match {
    case Some( sessionID ) => (session, checkSessionController( sessionID ))
    case None =>
      val sessionID = java.util.UUID.randomUUID().toString
      (session + ("sessionID" -> sessionID), checkSessionController( sessionID ))
  }

  private def checkSessionController( sessionID:String ):GameSession = _gameSessions.get( sessionID ) match {
    case Some( gameSession ) =>
      _gameSessions( sessionID ) = gameSession.update()
      gameSession
    case None =>
      val controller = _injector.getInstance( classOf[Controller] )
      findSaveGamePath( sessionID ).foreach( controller.loadGame )
      val gameSession = GameSession( controller )
      _gameSessions( sessionID ) = gameSession
      gameSession
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
}
