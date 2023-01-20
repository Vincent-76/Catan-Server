package model.socketcommands

import com.aimit.htwg.catan.model.DevelopmentCard
import model.{ FormData, GameSession, GameSocketCommand, InputForm, InvalidInput, SocketCommandScope, TypedGameSocketCommand }
import play.api.libs.json.JsValue

import scala.util.{ Failure, Try }

/**
 * @author Vincent76
 */
object UseDevCardCommand extends TypedGameSocketCommand[FormData[DevelopmentCard]]( "useDevCard", InputForm.singleForm( DevelopmentCard ), SocketCommandScope.Turn ) {
  override def typedGameExecute( gameSession:GameSession, sessionID:String, data:FormData[DevelopmentCard] ):Try[JsValue] =
    controllerAction( gameSession, _.action( _.useDevCard( data.value ) ) )
}
