package controllers

import com.aimit.htwg.catan.ClassicCatanModule
import com.aimit.htwg.catan.view.tui.TUI
import model.GameSession
import model.form.TUIInput
import play.api.i18n.{ I18nSupport, MessagesApi }
import play.api.mvc._

import javax.inject.{ Inject, Singleton }
import scala.util.{ Failure, Success }

/**
 * @author Vincent76
 */

@Singleton
class TUIController @Inject()( val sessionController:SessionController, val controllerComponents:ControllerComponents, val messagesAPI:MessagesApi ) extends BaseController with I18nSupport {

  private def getOrCreateGameSession( requestSession:Session ):(Session, GameSession) = {
    val gameSession = sessionController.getGameSession( requestSession )
    if( gameSession.isDefined )
      (requestSession, gameSession.get)
    else
      sessionController.newGameSession( requestSession, ClassicCatanModule.instance() )
  }

  def newGame( ):Action[AnyContent] = Action { implicit request:Request[AnyContent] =>
    sessionController.deleteGameSession( request.session )
    Redirect( routes.TUIController.game() )
  }

  def game():Action[AnyContent] = Action { implicit request:Request[AnyContent] =>
    val (session, gameSession) = getOrCreateGameSession( request.session )
    val inputForm = TUIInput.form.bindFromRequest()
    val (info, error, lines) = if( inputForm.value.isDefined )
      TUI.processInput( gameSession.controller, inputForm.get.input ) match {
        case (Success( info ), lines) => (info.map( i => TUI.getInfo( gameSession.controller, i ).toList ), None, lines)
        case (Failure( t ), lines) => (None, Some( TUI.getError( gameSession.controller, t ) ), lines)
      }
    else (None, None, Nil)
    val tuiState = TUI.findTUIState( gameSession.controller )
    val gameDisplay = tuiState.createGameDisplay
    val stateDisplay = tuiState.createStateDisplay
    val actionInfo = tuiState.getActionInfo
    Ok( views.html.tui( gameDisplay, stateDisplay, actionInfo, info, error, lines ) ).withSession( session )
  }
}


