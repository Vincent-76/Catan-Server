package model

import com.aimit.htwg.catan.model.{ NamedComponent, NamedComponentImpl }
import play.api.libs.json.JsValue

import scala.util.Try

/**
 * @author Vincent76
 */

object SocketCommand extends NamedComponent[SocketCommand]


abstract class SocketCommand( name:String ) extends NamedComponentImpl( name ) {
  override def init():Unit = SocketCommand.addImpl( this )

  def execute( gameSession:GameSession ):Try[JsValue]
}
