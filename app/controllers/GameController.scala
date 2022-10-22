package controllers

import akka.actor.ActorSystem
import model.GameData
import play.api.i18n.{ I18nSupport, MessagesApi }
import play.api.mvc.{ Action, AnyContent, BaseController, ControllerComponents, Request }

import javax.inject.{ Inject, Singleton }
import scala.concurrent.ExecutionContext

/**
 * @author Vincent76
 */

@Singleton
class GameController @Inject()( val sessionController:SessionController, val controllerComponents:ControllerComponents, val actorSystem:ActorSystem, val messagesAPI:MessagesApi )( implicit executionContext:ExecutionContext ) extends BaseController with I18nSupport {

  def game( ):Action[AnyContent] = Action { implicit request:Request[AnyContent] =>
    val (session, gameSession) = sessionController.getGameSession( request.session )
    Ok( views.html.game( GameData( gameSession ) ) ).withSession( session )
  }

}
