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


  def saveGameSession( session:Session ):Unit = session.get( "sessionID" ).foreach( sessionID =>
    gameSessions.find( _._1 == sessionID ).foreach( s =>
      s._2.controller.saveGame( Some( getSaveGamePath( sessionID ) ) )
    )
  )

  def loadGameSession( session:Session ):Unit = session.get( "sessionID" ).foreach( sessionID =>
    getGameSession( session ).foreach( gameSession =>
      findSaveGamePath( sessionID ).foreach( gameSession.controller.loadGame )
    )
  )



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

  def getGameSession( session:Session ):Option[GameSession] = session.get( "sessionID" ).flatMap( sessionID => {
    gameSessions.get( sessionID ) match {
      case Some( gameSession ) =>
        gameSessions( sessionID ) = gameSession.update()
        Some( gameSession )
      case None => findSaveGamePath( sessionID ).map( path => {
        val loaded = FileIO.load( path )
        val gameSession = GameSession( new Controller( loaded._2._1, loaded._1, new UndoManager( loaded._2._2, loaded._2._3 ) ) )
        gameSessions( sessionID ) = gameSession
        gameSession
      } );
    }
  } )

  def newGameSession( session:Session, catanModule:CatanModule ):(Session, GameSession) = session.get( "sessionID" ) match {
    case Some( sessionID ) =>
      deleteSaveGame( sessionID )
      (session, createNewGameSession( sessionID, catanModule ))
    case None =>
      val sessionID = java.util.UUID.randomUUID().toString
      (session + ("sessionID" -> sessionID), createNewGameSession( sessionID, catanModule ))
  }

  private def createNewGameSession( sessionID:String, catanModule:CatanModule ):GameSession = {
    val injector = Guice.createInjector( catanModule )
    val newGame = injector.getInstance( classOf[Game] )
    val game = newGame.state.initGame().flatMap( _.doStep( newGame ).toOption.map( _._1 ) ).get
    val gameSession = GameSession( new Controller( game, injector.getInstance( classOf[FileIO] ) ) )
    gameSessions( sessionID ) = gameSession
    gameSession
  }

  private def findSaveGames[R]( sessionID:String, f: List[java.io.File] => R ):Option[R] = {
    val dir = new java.io.File( CatanModule.savegamePath )
    if( dir.exists && dir.isDirectory ) {
      try {
        Some( f( dir.listFiles.filter( f => f.isFile && f.getName.matches( s"$sessionID\\..*" ) ).toList ) )
      } catch {
        case _:java.lang.SecurityException => None
      }
    } else None
  }

  private def hasSaveGame( sessionID:String ):Boolean =
    findSaveGames( sessionID, _.nonEmpty ).getOrElse( false )

  private def findSaveGamePath( sessionID:String ):Option[String] =
    findSaveGames( sessionID, _.headOption.map( _.getAbsolutePath ) ).flatten

  private def deleteSaveGame( sessionID:String ):Unit =
    findSaveGames( sessionID, _.foreach( _.delete() ) )
}
