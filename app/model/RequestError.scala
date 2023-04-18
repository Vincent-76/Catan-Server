package model

import com.aimit.htwg.catan.controller.Controller
import com.aimit.htwg.catan.model.{ AlreadyUsedDevCardInTurn, ControllerError, DevCardDrawnInTurn, DevStackIsEmpty, InconsistentData, InsufficientBankResources, InsufficientDevCards, InsufficientResources, InsufficientStructures, InvalidDevCard, InvalidPlacementPoint, InvalidPlayer, InvalidPlayerColor, InvalidPlayerID, InvalidResourceAmount, InvalidTradeResources, NoAdjacentStructure, NoConnectedStructures, NoPlacementPoints, NonExistentPlacementPoint, NotEnoughPlayers, NothingToRedo, NothingToUndo, PlacementPointNotEmpty, PlayerColorIsAlreadyInUse, PlayerNameAlreadyExists, PlayerNameEmpty, PlayerNameTooLong, RobberOnlyOnLand, SettlementRequired, TooCloseToBuilding, TradePlayerInsufficientResources, WrongState }

/**
 * @author Vincent76
 */

object RequestError {
  def apply( controller:Controller, t:Throwable ) = new RequestError( t match {
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
    case InvalidPlacementPoint( id ) => "Invalid placement point!"
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
    case e:ControllerError => "Unknown error!"
    case t:Throwable => t.toString
  } )
}

case class RequestError( message:String,
                         field:Option[String] = None
                       )
