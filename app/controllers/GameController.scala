package controllers

import akka.actor.ActorSystem
import com.aimit.htwg.catan.model.state.{ InitBeginnerState, InitPlayerState, InitState }
import model.GameData
import play.api.i18n.{ I18nSupport, MessagesApi }
import play.api.mvc.{ Action, AnyContent, BaseController, ControllerComponents, Request }

import javax.inject.{ Inject, Singleton }
import scala.concurrent.ExecutionContext

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
class GameController @Inject()( val sessionController:SessionController, val controllerComponents:ControllerComponents, val actorSystem:ActorSystem, val messagesAPI:MessagesApi )( implicit executionContext:ExecutionContext ) extends BaseController with I18nSupport {

  def newGame( ):Action[AnyContent] = Action { implicit request:Request[AnyContent] =>
    val (session, gameSession) = sessionController.getNewGameSession( request.session )
    Ok( views.html.game_setup( GameData( gameSession ) ) ).withSession( session )
  }

  def game( ):Action[AnyContent] = Action { implicit request:Request[AnyContent] =>
    val (session, gameSession) = sessionController.getGameSession( request.session )
    if( GameController.SETUP_STATES.exists( _.isInstance( gameSession.controller.game.state ) ) )
      Ok( views.html.game_setup( GameData( gameSession ) ) ).withSession( session )
    else
      Ok( views.html.game( GameData( gameSession ) ) ).withSession( session )
  }

}
