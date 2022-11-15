package controllers

import akka.actor.ActorSystem
import com.aimit.htwg.catan.controller.Controller
import com.aimit.htwg.catan.model.Info
import com.aimit.htwg.catan.model.state.{ InitBeginnerState, InitPlayerState, InitState }
import model.{ GameData, GameSession }
import model.form.{ AddPlayer, NewGame }
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

  private def showGame( errors:Map[String, List[String]] = Map.empty )( implicit request:RequestHeader ):Result = {
    val gameSession = sessionController.getGameSession( request.session )
    if( gameSession.isEmpty || GameController.SETUP_STATES.exists( _.isInstance( gameSession.get.controller.game.state ) ) )
      Ok( views.html.game_setup( gameSession.map( GameData( _ ) ), errors ) )
    else
      Ok( views.html.game( GameData( gameSession.get, errors ) ) )
  }

  private def showGameErrors( requestHeader: RequestHeader, errors:Map[String, List[String]] ):Result =
    showGame( errors )( requestHeader )


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



  private def gameAction( f:Controller => Try[Option[Info]] )( implicit request:RequestHeader ):Result = {
    val gameSession = sessionController.getGameSession( request.session )
    if( gameSession.isDefined ) {
      val res = f( gameSession.get.controller )
      // TODO
    }
    Redirect( routes.GameController.game() )
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
    gameAction( _.undoAction() )
  }

  def redo( ):Action[AnyContent] = Action { implicit request:Request[AnyContent] =>
    gameAction( _.redoAction() )
  }


  def addPlayer( ):Action[AddPlayer] = formAction( AddPlayer.form, showGameErrors ) { implicit request:Request[AddPlayer] =>
    gameAction( _.action( _.addPlayer( request.body.color, request.body.name ) ) )
  }

  def setInitBeginnerState( ):Action[AnyContent] = Action { implicit request:Request[AnyContent] =>
    gameAction( _.action( _.setInitBeginnerState() ) )
  }

  def diceOutBeginner( ):Action[AnyContent] = Action { implicit request:Request[AnyContent] =>
    gameAction( _.action( _.diceOutBeginner() ) )
  }

  def setBeginner( ):Action[AnyContent] = Action { implicit request:Request[AnyContent] =>
    gameAction( _.action( _.setBeginner() ) )
  }
}
