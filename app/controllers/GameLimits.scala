package controllers

import com.aimit.htwg.catan.model.impl.fileio.JsonSerializable
import com.aimit.htwg.catan.model.{ Game, Placement, PlayerColor }
import play.api.libs.json.{ JsValue, Json, Writes }

/**
 * @author Vincent76
 */
object GameLimits {
  def apply( game:Game ):GameLimits = new GameLimits(
    game.minPlayers,
    game.maxPlayers,
    game.requiredVictoryPoints,
    game.maxHandCards,
    game.defaultBankTradeFactor,
    game.unspecifiedPortFactor,
    game.specifiedPortFactor,
    game.maxPlayerNameLength
  )

  implicit val writes:Writes[GameLimits] = ( o:JsonSerializable ) => o.toJson
}

case class GameLimits( minPlayers:Int,
                       maxPlayers:Int,
                       requiredVictoryPoints:Int,
                       maxHandCards:Int,
                       defaultBankTradeFactor:Int,
                       unspecifiedPortFactor:Int,
                       specifiedPortFactor:Int,
                       maxPlayerNameLength:Int
                     ) extends JsonSerializable {

  override def toJson:JsValue = Json.obj(
    "minPlayers" -> Json.toJson( minPlayers ),
    "maxPlayers" -> Json.toJson( maxPlayers ),
    "requiredVictoryPoints" -> Json.toJson( requiredVictoryPoints ),
    "maxHandCards" -> Json.toJson( maxHandCards ),
    "defaultBankTradeFactor" -> Json.toJson( defaultBankTradeFactor ),
    "unspecifiedPortFactor" -> Json.toJson( unspecifiedPortFactor ),
    "specifiedPortFactor" -> Json.toJson( specifiedPortFactor ),
    "maxPlayerNameLength" -> Json.toJson( maxPlayerNameLength ),
  )
}
