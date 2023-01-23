package model

import com.aimit.htwg.catan.model.impl.fileio.JsonSerializable
import com.aimit.htwg.catan.model.impl.fileio.JsonFileIO.JsonMap
import com.aimit.htwg.catan.model.{ BonusCard, DevelopmentCard, Game }
import play.api.libs.json.{ JsValue, Json, Writes }

/**
 * @author Vincent76
 */
object GameValues {
  def apply( game:Game ):GameValues = new GameValues(
    game.minPlayers,
    game.maxPlayers,
    game.requiredVictoryPoints,
    game.maxHandCards,
    game.defaultBankTradeFactor,
    game.unspecifiedPortFactor,
    game.specifiedPortFactor,
    game.maxPlayerNameLength,
    DevelopmentCard.impls.map( d => (d, (d.usable, d.desc)) ).toMap,
    BonusCard.impls.map( c => (c, c.bonus) ).toMap
  )

  implicit val writes:Writes[GameValues] = ( o:JsonSerializable ) => o.toJson
}

case class GameValues( minPlayers:Int,
                       maxPlayers:Int,
                       requiredVictoryPoints:Int,
                       maxHandCards:Int,
                       defaultBankTradeFactor:Int,
                       unspecifiedPortFactor:Int,
                       specifiedPortFactor:Int,
                       maxPlayerNameLength:Int,
                       devCards:Map[DevelopmentCard, (Boolean, String)],
                       bonusCardVictoryPoints:Map[BonusCard, Int]
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
    "devCards" -> devCards.toJsonC( Json.toJson( _ ), v => Json.obj(
      "usable" -> Json.toJson( v._1 ),
      "desc" -> Json.toJson( v._2 )
    ) ),
    "bonusCardVictoryPoints" -> bonusCardVictoryPoints.toJson // Json.toJson( bonusCardVictoryPoints )
  )
}
