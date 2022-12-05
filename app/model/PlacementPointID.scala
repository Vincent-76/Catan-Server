package model

import com.aimit.htwg.catan.model.impl.fileio.{ JsonDeserializer, JsonSerializable }
import play.api.libs.json.{ JsValue, Json }

/**
 * @author Vincent76
 */

object PlacementPointID extends JsonDeserializer[PlacementPointID] {
  override def fromJson( json:JsValue ):PlacementPointID = {
    val id = ( json \ "id" ).as[Int]
    ( json \ "class" ).as[String] match {
      case "Hex" => HexPlacementPoint( id )
      case "Edge" => EdgePlacementPoint( id )
      case "Vertex" => VertexPlacementPoint( id )
    }
  }
}

sealed abstract class PlacementPointID( val name:String, val id:Int ) extends JsonSerializable {
  override def toJson:JsValue = Json.obj(
    "class" -> name,
    "id" -> Json.toJson( id )
  )
}


case class HexPlacementPoint( hID:Int ) extends PlacementPointID( "Hex", hID )

case class EdgePlacementPoint( eID:Int ) extends PlacementPointID( "Edge", eID )

case class VertexPlacementPoint( vID:Int ) extends PlacementPointID( "Vertex", vID )
