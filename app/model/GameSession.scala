package model

import com.aimit.htwg.catan.controller.Controller
import com.aimit.htwg.catan.model.GameField.Field
import com.aimit.htwg.catan.model.{ Hex, Resource }
import util.RichIterable

import scala.util.Random

/**
 * @author Vincent76
 */

object GameSession {
  def apply( controller:Controller ):GameSession = GameSession(
    controller,
    getShuffledImageIDs( controller.game.gameField.field )
  )

  private def getShuffledImageIDs( field:Field[Hex] ):Map[Resource, List[String]] = {
    val resourceVariantsAndCount:Map[Resource, (List[String], Int)] =
      field.foldLeft( Map.empty[Resource, (List[String], Int)] )( ( data, row ) =>
        row.mapWhere( h => h.isDefined && h.get.area.f.isInstanceOf[Resource], _.get.area.f.asInstanceOf[Resource] )
          .foldLeft( data )( ( data, r ) => data.get( r ) match {
            case Some( entry ) => data + ( r -> (entry._1, entry._2 + 1) )
            case None => data + ( r -> (getResourceImageVariants( r ), 1) )
          } )
      )
    resourceVariantsAndCount.map( e => (
      e._1,
      Random.shuffle( (0 until e._2._2).map( i => e._2._1( i % e._2._1.length ) ).toList )
    ) )
  }

  private def getResourceImageVariants( r:Resource ):List[String] = {
    try {
      val dir = new java.io.File( s"public/images/${r.title.toLowerCase}" )
      if( dir.exists() && dir.isDirectory )
        dir.listFiles().toList.map( _.getName )
      else Nil
    } catch {
      case _:java.lang.SecurityException => Nil
    }
  }
}

case class GameSession( controller:Controller, resourceImages:Map[Resource, List[String]], timestamp:Long = java.time.Instant.now().getEpochSecond ) {

  def update():GameSession = copy( controller, resourceImages )
}