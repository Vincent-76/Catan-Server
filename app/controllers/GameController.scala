package controllers

import akka.actor.ActorSystem
import com.aimit.htwg.catan.controller.Controller
import com.aimit.htwg.catan.model.state.{ InitBeginnerState, InitPlayerState, InitState, NextPlayerState }
import com.aimit.htwg.catan.model.{ Command, Info, State }
import model.GameData
import model.form.{ AddPlayer, NewGame }
import play.api.data.Form
import play.api.i18n.MessagesApi
import play.api.mvc._

import javax.inject.{ Inject, Singleton }
import scala.concurrent.ExecutionContext
import scala.util.Try

/**
 * @author Vincent76
 */

object GameController {
  val SETUP_STATES:List[Class[_]] = List(
    classOf[InitState],
    classOf[InitPlayerState],
    classOf[InitBeginnerState]
  )
}

@Singleton
class GameController @Inject()( controllerComponents:ControllerComponents,
                                val sessionController:SessionController,
                                val actorSystem:ActorSystem,
                                val messagesAPI:MessagesApi
                              )( implicit executionContext:ExecutionContext ) extends CatanBaseController( controllerComponents ) {

  private def showGame( info:Option[Info] = None, errors:Map[String, List[String]] = Map.empty )( implicit request:RequestHeader ):Result = {
    val gameSession = sessionController.getGameSession( request.session )
    gameSession.foreach( s => println( s.controller.game.state.getClass.getSimpleName ) )
    if( gameSession.isEmpty || GameController.SETUP_STATES.exists( _.isInstance( gameSession.get.controller.game.state ) ) )
      Ok( views.html.game_setup( gameSession.map( GameData( _ ) ), errors ) )
    else
      Ok( views.html.game( GameData( gameSession.get ), info, errors ) )
  }

  private def showGameErrors( requestHeader: RequestHeader, errors:Map[String, List[String]] ):Result =
    showGame( errors = errors )( requestHeader )


  def newGame( ):Action[AnyContent] = Action { implicit request:Request[AnyContent] =>
    sessionController.deleteGameSession( request.session )
    Redirect( routes.GameController.game() )
  }

  def createGame( ):Action[NewGame] = formAction( NewGame.form, showGameErrors ) { implicit request:Request[NewGame] =>
    sessionController.deleteGameSession( request.session )
    val module = request.body.module.instance( fileIO = request.body.fileIO, availablePlacements = request.body.availablePlacements )
    val (session, _) = sessionController.newGameSession( request.session, module )
    Redirect( routes.GameController.game() ).withSession( session )
  }

  def game( ):Action[AnyContent] = Action { implicit request:Request[AnyContent] =>
    showGame()
  }

  private def controllerAction( f:Controller => Try[Option[Info]], undo:Boolean = false )( implicit request:Request[_] ):Result = {
    val gameSession = sessionController.getGameSession( request.session )
    if( gameSession.isDefined ) {
      val result = f( gameSession.get.controller )
      val res = if( gameSession.get.controller.game.state.isInstanceOf[NextPlayerState] ) {
        if( undo )
          gameSession.get.controller.undoAction()
        else {
          gameSession.get.controller.action( _.startTurn() )
          result
        }
      } else result
      if( res.isSuccess && res.get.isDefined )
        showGame( res.get )
      else
        Redirect( routes.GameController.game() )
      // TODO
    } else Redirect( routes.GameController.game() )
  }

  private def gameAction( f:State => Option[Command] ):Action[AnyContent] = Action { implicit request:Request[AnyContent] =>
    controllerAction( _.action( f ) )
  }

  private def gameFormAction[F]( form:Form[F],
                                 onErrors:(RequestHeader, Map[String, List[String]]) => Result,
                                 f:(F, State) => Option[Command]
                               ) = formAction[F]( form, onErrors ) { implicit request:Request[F] =>
    controllerAction( _.action( s => f( request.body, s ) ) )
  }


  def saveGame( ):Action[AnyContent] = Action { implicit request:Request[AnyContent] =>
    sessionController.saveGameSession( request.session )
    Redirect( routes.GameController.game() )
  }

  def loadGame( ):Action[AnyContent] = Action { implicit request:Request[AnyContent] =>
    sessionController.loadGameSession( request.session )
    Redirect( routes.GameController.game() )
  }

  def undo( ):Action[AnyContent] = Action { implicit request:Request[AnyContent] =>
    controllerAction( _.undoAction(), undo = true )
  }

  def redo( ):Action[AnyContent] = Action { implicit request:Request[AnyContent] =>
    controllerAction( _.redoAction() )
  }


  def addPlayer( ):Action[AddPlayer] = gameFormAction[AddPlayer]( AddPlayer.form, showGameErrors, ( data, state ) =>
    state.addPlayer( data.color, data.name ) )

  def setInitBeginnerState( ):Action[AnyContent] = gameAction( _.setInitBeginnerState() )

  def diceOutBeginner( ):Action[AnyContent] = gameAction( _.diceOutBeginner() )

  def setBeginner( ):Action[AnyContent] = gameAction( _.setBeginner() )

  def placeRobber( hID:Int ):Action[AnyContent] = gameAction( _.placeRobber( hID ) )

  def build( id:Int ):Action[AnyContent] = gameAction( _.build( id ) )

  def rollTheDices( ):Action[AnyContent] = gameAction( _.rollTheDices() )
}
