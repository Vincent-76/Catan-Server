package model

import com.aimit.htwg.catan.controller.Controller
import com.aimit.htwg.catan.model._

/**
 * @author Vincent76
 */

object SocketError {
  def fromControllerError( controller:Controller, t:Throwable ):SocketError = new GameError( t match {
    case WrongState => "Unable in this state!"
    case InsufficientResources => "Insufficient resources for this action!"
    case TradePlayerInsufficientResources => "Trade player has insufficient resources for this action!"
    case InsufficientStructures( structure ) =>
      "Insufficient structures of " + structure.name + " for this action!"
    case NonExistentPlacementPoint( id ) => "This placement point does not exists!"
    case PlacementPointNotEmpty( id ) => "This placement point is not empty!"
    case NoAdjacentStructure => "Player has no adjacent structure!"
    case TooCloseToBuilding( id ) => "This placement point is too close to another building!"
    case NoConnectedStructures( id ) => "No connected structures on this placement point!"
    case SettlementRequired( id ) =>
      "You need a settlement on this placement point to build a city!"
    case InvalidInput( id ) => "Invalid placement point!"
    case NotEnoughPlayers => "Minimum " + controller.game.minPlayers + " players required!"
    case InvalidPlayerColor( color ) => "Invalid player color: [" + color + "]!"
    case RobberOnlyOnLand => "Robber can only be placed on land!"
    case NoPlacementPoints( structure ) =>
      "No available placement points for structure " + structure.name + "!"
    case InvalidResourceAmount( amount ) => "Invalid resource amount: " + amount + "!"
    case InvalidTradeResources( give, get ) =>
      "Invalid trade resources: " + give.name + " <-> " + get.name + "!"
    case InvalidDevCard( devCard ) => "Invalid dev card: [" + devCard + "]!"
    case InsufficientDevCards( devCard ) => "Insufficient dev cards of " + devCard.name + "!"
    case AlreadyUsedDevCardInTurn => "You already used a development card in this turn!"
    case DevCardDrawnInTurn( devCard ) =>
      "You've drawn this development card (" + devCard.name + ") in this turn, you can use it in your next turn."
    case InsufficientBankResources => "Bank has insufficient resources!"
    case InconsistentData => "Internal problem, please try again."
    case DevStackIsEmpty => "Development card stack is empty!"
    case PlayerNameAlreadyExists( name ) => "Player with name: [" + name + "] already exists!"
    case PlayerNameEmpty => "Player name can't be empty!"
    case PlayerNameTooLong( name ) =>
      "Player name [" + name + "] is too long, maximum " + controller.game.maxPlayerNameLength + " characters!"
    case PlayerColorIsAlreadyInUse( playerColor ) =>
      "Player color: [" + playerColor.name + "] is already in use!"
    case InvalidPlayerID( id ) => "Invalid player id: [" + id + "]!"
    case InvalidPlayer( playerID ) => "Invalid player with id: " + playerID + "!"
    case NothingToUndo => "Nothing to undo!"
    case NothingToRedo => "Nothing to redo!"
    case e:ControllerError => "Unknown error: [" + e.getClass.getSimpleName + "]: " + e.getMessage
    case t:Throwable => t + ": " + t.getMessage
  } )
}

class SocketError( val code:Int, error:String ) extends Throwable( error )

case object NoAction extends SocketError( 0, "NoAction" )

case object InvalidCommand extends SocketError( 1, "InvalidCommand" )

case object NoGame extends SocketError( 2, "NoGame" )

case class GameError( error:String ) extends SocketError( 3, error )

case object NoSessionFound extends SocketError( 4, "NoSessionFound" )

case object AlreadyRegistered extends SocketError( 5, "AlreadyRegistered" )

case object NotPossibleAnymore extends SocketError( 6, "NotPossibleAnymore" )

case object GameAlreadyFull extends SocketError( 7, "GameAlreadyFull" )

case object Forbidden extends SocketError( 8, "Forbidden" )

case class InvalidInput( input:String ) extends SocketError( 9, s"Invalid Input: $input" )