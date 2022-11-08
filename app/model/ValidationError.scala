package model

import scala.language.implicitConversions

/**
 * @author Vincent76
 */

object ValidationError extends Enumeration {
  type Error = Value
  implicit def valToErrorDetails( v:Value ):E = v.asInstanceOf[E]
  case class E( i:Int, name:String, message:String ) extends super.Val( i, name )

  def of( name:String ):Option[Error] = values.find( _.name == name )

  def msg( name:String ):String = of( name ).map( _.message ).getOrElse( "Error!" )

  val Required:E = E( 0, "error.required", "Required!" )
  val Empty:E = E( 1, "empty", "Can't be nothing!" )
}
