package controllers

import akka.actor.ActorSystem
import com.aimit.htwg.catan.model.state.{ InitBeginnerState, InitPlayerState, InitState }
import model.GameData
import model.form.NewGame
import play.api.data.FormError
import play.api.i18n.MessagesApi
import play.api.mvc._

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
class GameController @Inject()( controllerComponents:ControllerComponents,
                                val sessionController:SessionController,
                                val actorSystem:ActorSystem,
                                val messagesAPI:MessagesApi
                              )( implicit executionContext:ExecutionContext ) extends CatanBaseController( controllerComponents ) {

  private def showGame( errors:Map[String, List[String]] = Map.empty )( implicit request:RequestHeader ):Result = {
    val (session, gameSession) = sessionController.getGameSession( request.session )
    if( gameSession.isEmpty || GameController.SETUP_STATES.exists( _.isInstance( gameSession.get.controller.game.state ) ) )
      Ok( views.html.game_setup( gameSession.map( GameData( _ ) ), errors ) ).withSession( session )
    else
      Ok( views.html.game( GameData( gameSession.get, errors ) ) ).withSession( session )
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
    sessionController.newGameSession( request.session, module )
    Redirect( routes.GameController.game() )
  }

  def game( ):Action[AnyContent] = Action { implicit request:Request[AnyContent] =>
    showGame()
  }

}
