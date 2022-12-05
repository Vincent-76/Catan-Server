package model

/**
 * @author Vincent76
 */

sealed abstract class SocketError( error:String ) extends Throwable( error )

object InvalidCommand extends SocketError( "InvalidCommand" )

object NoSession extends SocketError( "NoSession" )
