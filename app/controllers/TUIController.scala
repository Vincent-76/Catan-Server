package controllers

import akka.actor.ActorSystem
import com.aimit.htwg.catan.CatanModule
import com.aimit.htwg.catan.controller.Controller
import com.aimit.htwg.catan.view.tui.TUI
import com.google.inject.{ Guice, Injector }
import model.{ InputForm, GameSession }
import play.api.i18n.{ I18nSupport, MessagesApi }
import play.api.mvc._
import play.twirl.api.Html

import javax.inject.{ Inject, Singleton }
import scala.collection.mutable
import scala.concurrent.ExecutionContext
import scala.concurrent.duration.DurationInt
import scala.util.{ Failure, Success }

/**
 * @author Vincent76
 */

@Singleton
class TUIController @Inject()( val sessionController:SessionController, val controllerComponents:ControllerComponents, val messagesAPI:MessagesApi ) extends BaseController with I18nSupport {

  def game( ):Action[AnyContent] = Action { implicit request:Request[AnyContent] =>
    val (session, gameSession) = sessionController.getGameSession( request.session )
    val tuiState = TUI.findTUIState( gameSession.controller )
    Ok( views.html.tui( tuiState.createGameDisplay, tuiState.createStateDisplay, tuiState.getActionInfo ) ).withSession( session )
  }


  def input( ):Action[AnyContent] = Action { implicit request:Request[AnyContent] =>
    val inputForm = InputForm.form.bindFromRequest().get
    val (session, gameSession) = sessionController.getGameSession( request.session )
    val (info, error, lines) = TUI.processInput( gameSession.controller, inputForm.input ) match {
      case (Success( info ), lines) => (info.map( i => TUI.getInfo( gameSession.controller, i ).toList ), None, lines)
      case (Failure( t ), lines) => (None, Some( TUI.getError( gameSession.controller, t ) ), lines)
    }
    val tuiState = TUI.findTUIState( gameSession.controller )
    val gameDisplay = tuiState.createGameDisplay
    val stateDisplay = tuiState.createStateDisplay
    val actionInfo = tuiState.getActionInfo
    Ok( views.html.tui( gameDisplay, stateDisplay, actionInfo, info, error, lines ) ).withSession( session )
  }
}


