package model

import com.aimit.htwg.catan.controller.Controller
import com.aimit.htwg.catan.model.{ Edge, Game, Hex, Vertex }
import com.aimit.htwg.catan.model.impl.fileio.JsonSerializable
import com.aimit.htwg.catan.model.impl.placement.{ RoadPlacement, RobberPlacement, SettlementPlacement }
import com.aimit.htwg.catan.model.state.{ BuildInitRoadState, BuildInitSettlementState, BuildState, DevRoadBuildingState, RobberPlaceState }
import play.api.libs.json.{ JsValue, Json, Writes }

/**
 * @author Vincent76
 */

object GameStatus {
  def apply( controller:Controller ):GameStatus = new GameStatus(
    controller.hasUndo,
    controller.hasRedo,
    getBuildablePoints( controller.game )
  )

  implicit val writes:Writes[GameStatus] = ( o:JsonSerializable ) => o.toJson

  private def getBuildablePoints( game:Game ):List[PlacementPointID] = (game.state match {
    case RobberPlaceState( _ ) => RobberPlacement.getBuildablePoints( game, game.player.id )
    case BuildInitSettlementState() => SettlementPlacement.getBuildablePoints( game, game.player.id, any = true )
    case BuildInitRoadState( vID ) => game.getBuildableRoadSpotsForSettlement( vID )
    case BuildState( structure ) => structure.getBuildablePoints( game, game.player.id )
    case DevRoadBuildingState( _, _ ) => RoadPlacement.getBuildablePoints( game, game.player.id )
    case _ => Nil
  }).map {
    case h:Hex => HexPlacementPoint( h.id )
    case e:Edge => EdgePlacementPoint( e.id )
    case v:Vertex => VertexPlacementPoint( v.id )
  }
}

case class GameStatus( hasUndo:Boolean,
                       hasRedo:Boolean,
                       buildablePoints:List[PlacementPointID],
                     ) extends JsonSerializable {

  override def toJson:JsValue = Json.obj(
    "hasUndo" -> Json.toJson( hasUndo ),
    "hasRedo" -> Json.toJson( hasRedo ),
    "buildablePoints" -> Json.toJson( buildablePoints.map( _.toJson ) )
  )

}
