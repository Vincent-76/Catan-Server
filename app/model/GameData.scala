package model

import com.aimit.htwg.catan.model.impl.fileio.JsonSerializable
import com.aimit.htwg.catan.model.impl.placement.{ RoadPlacement, RobberPlacement, SettlementPlacement }
import com.aimit.htwg.catan.model.state._
import com.aimit.htwg.catan.model._
import com.aimit.htwg.catan.util.RichOption
import play.api.libs.json.{ JsValue, Json }

import scala.collection.mutable
import scala.math.cos

/**
 * @author Vincent76
 */

object GameData { // extends JsonDeserializer[GameData] {
  def apply( gameSession:GameSession ):GameData = new GameData(
    gameSession.controller.game,
    gameSession.controller.hasUndo,
    gameSession.controller.hasRedo,
    gameSession.resourceImages,
    getBuildablePoints( gameSession.controller.game )
  )

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

  /*override def fromJson( json:JsValue ):GameData = GameData(
    game = ( json \ "game" ).as[Game]
  )*/
}


case class GameData( game:Game,
                     hasUndo:Boolean,
                     hasRedo:Boolean,
                     resourceImages:Map[Resource, List[Int]],
                     buildablePoints:List[PlacementPointID],
                     resourceCounter:mutable.Map[Resource, Int] = mutable.Map(),
                   ) extends JsonSerializable {

  override def toJson:JsValue = Json.obj(
    "game" -> Json.toJson( game ),
    "hasUndo" -> Json.toJson( hasUndo ),
    "hasRedo" -> Json.toJson( hasRedo ),
    "resourceImages" -> Json.toJson( resourceImages ),
    "buildablePoints" -> Json.toJson( buildablePoints.map( _.toJson ) )
  )

  def gameField:GameField = game.gameField

  def aspectRatio:Double =
    gameField.fieldWidth / ( 1 + ( gameField.fieldHeight - 1 ) * 0.75 ) * cos( 30.toRadians )

  def imagePath( hex:Hex ):String = {
    //print( s"ID: ${hex.id}, area: ${hex.area.f.title}" )
    val imagePath = hex.area match {
      case _:DesertArea => "desert.png"
      case WaterArea( None ) => "water.png"
      case WaterArea( Some( port ) ) =>
        val rotNr = GameField.adjacentOffset.indices.find( i => gameField.adjacentEdge( hex, i ) match {
          case Some( e ) if e.port.isDefined && e.port.get == port => true
          case _ => false
        } ).getOrElse( 0 )
        port.specific match {
          case Some( r ) => s"ports/${r.name.toLowerCase}/$rotNr.png"
          case None => s"ports/unspecific/$rotNr.png"
        }
      case area:ResourceArea =>
        val i = resourceCounter.getOrElse( area.resource, 0 )
        resourceCounter( area.resource ) = i + 1
        //print( s" i: $i next: ${resourceCounter( area.resource )}" )

        resourceImages.getOrElse( area.resource, List.empty ).lift( i ).useOrElse(
          s => s"${area.resource.name.toLowerCase}/$s.png",
          s"${area.resource.name.toLowerCase}.png"
        )
    }
    //println( " path: " + imagePath )
    s"images/$imagePath"
  }

  def hexClass( hex:Hex ):String = hex.area match {
    case _:WaterArea => "waterArea"
    case _:DesertArea => "desert landArea"
    case r:ResourceArea => r.resource.name + " resourceArea landArea"
  }

  def getEdge( hex:Hex, offsetIndex:Int ):Option[Edge] =
    gameField.adjacentEdge( hex, offsetIndex )

  def getVertex( hex:Hex, offsetIndex1:Int, offsetIndex2:Int ):Option[Vertex] =
    gameField.adjacentVertex( hex, offsetIndex1, offsetIndex2 )

  def structureDisplayClass( structure:Option[Structure] ):String = structure match {
    case Some( _ ) => "structureBuilt"
    case None => "structureNotBuilt"
  }

  def structureColorClass( structure:Option[Structure] ):String = structure match {
    case Some( s ) => pBGColorClass( s.owner )
    case None => "transparent"
  }

  def pBGColorClass( pID:PlayerID ):String = pBGColorClass( game.player( pID ).color )

  def pBGColorClass( color:PlayerColor ):String = color match {
    case Green => "pBGColorGreen"
    case Blue => "pBGColorBlue"
    case Yellow => "pBGColorYellow"
    case Red => "pBGColorRed"
  }

  def pTextColorClass( pID:PlayerID ):String = pTextColorClass( game.player( pID ).color )

  def pTextColorClass( color:PlayerColor ):String = color match {
    case Green => "pTextColorGreen"
    case Blue => "pTextColorBlue"
    case Yellow => "pTextColorYellow"
    case Red => "pTextColorRed"
  }

  def pBorderColorClass( pID:PlayerID ):String = pBorderColorClass( game.player( pID ).color )

  def pBorderColorClass( color:PlayerColor ):String = color match {
    case Green => "pBorderColorGreen"
    case Blue => "pBorderColorBlue"
    case Yellow => "pBorderColorYellow"
    case Red => "pBorderColorRed"
  }

  def buildingClass( building:Option[Building] ):String = building match {
    case Some( _:Settlement ) => "settlement"
    case Some( _:City ) => "city"
    case _ => ""
  }
}
