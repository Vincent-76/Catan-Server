package controllers

import akka.actor.ActorSystem
import com.aimit.htwg.catan.CatanModule
import com.aimit.htwg.catan.controller.Controller
import com.aimit.htwg.catan.view.tui.TUI
import com.google.inject.{ Guice, Injector }
import model.{ InputForm, SessionController }
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
class TUIController @Inject()( val controllerComponents:ControllerComponents, val actorSystem:ActorSystem, val messagesAPI:MessagesApi )( implicit executionContext:ExecutionContext ) extends BaseController with I18nSupport {
  val injector:Injector = Guice.createInjector( new CatanModule( test = false ) )
  val sessionControllers:mutable.Map[String, SessionController] = mutable.Map()


  actorSystem.scheduler.scheduleAtFixedRate( 1.minutes, 1.minutes ) { () =>
    val now = java.time.Instant.now().getEpochSecond
    sessionControllers.filterInPlace( ( _, v ) => ( now - v.timestamp ) < 60 * 60 )
  }


  def game( ):Action[AnyContent] = Action { implicit request:Request[AnyContent] =>
    val (session, controller) = _getSessionController( request.session )
    val tuiState = TUI.findTUIState( controller )
    Ok( views.html.tui( tuiState.createGameDisplay, tuiState.createStateDisplay, tuiState.getActionInfo ) ).withSession( session )
  }

  def _getSessionController( session:Session ):(Session, Controller) = session.get( "sessionID" ) match {
    case Some( sessionID ) => (session, _checkSessionController( sessionID ))
    case None =>
      val sessionID = java.util.UUID.randomUUID().toString
      (session + ("sessionID" -> sessionID), _checkSessionController( sessionID ))
  }

  def _checkSessionController( sessionID:String ):Controller = sessionControllers.get( sessionID ) match {
    case Some( sessionController ) =>
      sessionControllers( sessionID ) = sessionController.update()
      sessionController.controller
    case None =>
      val controller = injector.getInstance( classOf[Controller] )
      sessionControllers( sessionID ) = SessionController( controller )
      controller
  }



  def input( ):Action[AnyContent] = Action { implicit request:Request[AnyContent] =>
    val inputForm = InputForm.form.bindFromRequest().get
    val (session, controller) = _getSessionController( request.session )
    val (info, error, lines) = TUI.processInput( controller, inputForm.input ) match {
      case (Success( info ), lines) => (info.map( i => TUI.getInfo( controller, i ).toList ), None, lines)
      case (Failure( t ), lines) => (None, Some( TUI.getError( controller, t ) ), lines)
    }
    val tuiState = TUI.findTUIState( controller )
    val gameDisplay = tuiState.createGameDisplay
    val stateDisplay = tuiState.createStateDisplay
    val actionInfo = tuiState.getActionInfo
    Ok( views.html.tui( gameDisplay, stateDisplay, actionInfo, info, error, lines ) ).withSession( session )
  }
}


