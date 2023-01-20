package model.socketcommands

import com.aimit.htwg.catan.model.StructurePlacement
import model._
import play.api.libs.json.JsValue

import scala.util.Try

/**
 * @author Vincent76
 */
object SetBuildStateCommand extends TypedGameSocketCommand( "setBuildState", InputForm.singleForm( StructurePlacement ), SocketCommandScope.Turn ) {

  override def typedGameExecute( gameSession:GameSession, sessionID:String, data:FormData[StructurePlacement] ):Try[JsValue] =
    controllerAction( gameSession, _.action( _.setBuildState( data.value ) ) )
}