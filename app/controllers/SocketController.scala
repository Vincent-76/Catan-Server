package controllers

import akka.actor.{ ActorSystem, Props }
import akka.stream.Materializer
import play.api.libs.streams.ActorFlow

import javax.inject.{ Inject, Singleton }
import play.api.mvc.{ AbstractController, Action, AnyContent, ControllerComponents, Request, WebSocket }

import java.util.UUID
import scala.concurrent.Future
import scala.util.{ Failure, Success, Try }

/**
 * @author Vincent76
 */

@Singleton
class SocketController @Inject()( controllerComponents:ControllerComponents,
                                  val sessionController:SessionController,
                                )( implicit actorSystem:ActorSystem,
                                   mat:Materializer
                                ) extends AbstractController( controllerComponents ) {

  def init( ):Action[AnyContent] = Action { implicit request:Request[AnyContent] =>
    if( request.session.get( "sessionID" ).isEmpty )
      Ok.withSession( request.session + ("sessionID" -> java.util.UUID.randomUUID().toString) )
    Ok
  }


  def webSocket( sessionID:String ):WebSocket = WebSocket.acceptOrResult[String, String] { request =>
    Future.successful( Try{ UUID.fromString( sessionID ) } match {
      case Failure( _ ) =>
        println( "Invalid sessionID!" )
        Left( Forbidden )
      case Success( _ ) => Right( ActorFlow.actorRef { out =>
        Props( CatanWebSocketActor( out, sessionController, sessionID ) )
      } )
    } )
  }
}

