package model

import com.aimit.htwg.catan.model.{ Blue, Building, City, DesertArea, Game, GameField, Green, Hex, PlayerID, Red, Resource, ResourceArea, Road, Settlement, Structure, WaterArea, Yellow }
import com.aimit.htwg.catan.util.RichOption

import scala.collection.mutable
import scala.math.cos

/**
 * @author Vincent76
 */

object GameData {
  def apply( gameSession:GameSession ):GameData = GameData(
    gameSession.controller.game,
    gameSession.controller.hasUndo,
    gameSession.controller.hasRedo,
    gameSession.resourceImages
  )
}


case class GameData( game:Game,
                     hasUndo:Boolean,
                     hasRedo:Boolean,
                     resourceImages:Map[Resource, List[String]],
                     resourceCounter:mutable.Map[Resource, Int] = mutable.Map()
                   ) {

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
          case Some( r ) => s"ports/${r.title.toLowerCase}/$rotNr.png"
          case None => s"ports/unspecific/$rotNr.png"
        }
      case area:ResourceArea =>
        val i = resourceCounter.getOrElse( area.resource, 0 )
        resourceCounter( area.resource ) = i + 1
        //print( s" i: $i next: ${resourceCounter( area.resource )}" )
        resourceImages.getOrElse( area.resource, List.empty ).lift( i ).useOrElse(
          s => s"${area.resource.title.toLowerCase}/$s",
          s"${area.resource.title.toLowerCase}.png"
        )
    }
    //println( " path: " + imagePath )
    s"images/$imagePath"
  }

  def hexClass( hex:Hex ):String = hex.area match {
    case _:WaterArea => "waterArea"
    case _:DesertArea => "desert landArea"
    case r:ResourceArea => r.resource.title + " resourceArea landArea"
  }

  def getRoad( hex:Hex, offsetIndex:Int ):Option[Road] =
    gameField.adjacentEdge( hex, offsetIndex ).flatMap( _.road )

  def getBuilding( hex:Hex, offsetIndex1:Int, offsetIndex2:Int ):Option[Building] =
    gameField.adjacentVertex( hex, offsetIndex1, offsetIndex2 ).flatMap( _.building )

  def structureDisplayClass( structure:Option[Structure] ):String = structure match {
    case Some( _ ) => "structureBuilt"
    case None => "structureNotBuilt"
  }

  def structureColor( structure:Option[Structure] ):String = structure match {
    case Some( s ) => playerColor( s.owner )
    case None => "transparent"
  }


  def playerColor( pID:PlayerID ):String = game.player( pID ).color match {
    case Green => "green"
    case Blue => "blue"
    case Yellow => "yellow"
    case Red => "red"
  }

  def buildingClass( building:Option[Building] ):String = building match {
    case Some( _:Settlement ) => "settlement"
    case Some( _:City ) => "city"
    case None => ""
  }
}
