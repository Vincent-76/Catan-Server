package controllers

import akka.actor.{ ActorSystem, Props }
import akka.stream.Materializer
import play.api.libs.streams.ActorFlow

import javax.inject.{ Inject, Singleton }
import play.api.mvc.{ AbstractController, ControllerComponents, WebSocket }

import scala.concurrent.Future

/**
 * @author Vincent76
 */

@Singleton
class SocketController @Inject()( controllerComponents:ControllerComponents,
                                  val sessionController:SessionController,
                                )( implicit actorSystem:ActorSystem,
                                   mat:Materializer
                                ) extends AbstractController( controllerComponents ) {


  def webSocket:WebSocket = WebSocket.acceptOrResult[String, String] { request =>
    Future.successful( request.session.get( "sessionID" ) match {
      case None => Left( Forbidden )
      case Some( sessionID ) => Right( ActorFlow.actorRef { out =>
        Props( CatanWebSocketActor( out, sessionController, sessionID ) )
      } )
    } )
  }
}

