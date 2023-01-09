package model

import com.aimit.htwg.catan.controller.Controller
import com.aimit.htwg.catan.model.GameField.Field
import com.aimit.htwg.catan.model.{ Hex, PlayerID, Resource }
import util.RichIterable

import java.util.UUID
import scala.util.Random

/**
 * @author Vincent76
 */

object GameSession {
  def apply( hostID:String, controller:Controller ):GameSession = new GameSession(
    UUID.randomUUID().toString,
    hostID,
    Map( hostID -> None ),
    controller,
    getShuffledImageIDs( controller.game.gameField.field )
  )

  private def getShuffledImageIDs( field:Field[Hex] ):Map[Resource, List[Int]] = {
    field.foldLeft( Map.empty[Resource, Int] )( ( data, row ) =>
      row.mapWhere( h => h.isDefined && h.get.area.f.isInstanceOf[Resource], _.get.area.f.asInstanceOf[Resource] )
        .foldLeft( data )( ( data, r ) => data.get( r )  match {
          case Some( count ) => data + ( r -> ( count + 1 ) )
          case None => data + ( r -> 1 )
        } )
    ).map( e => (
      e._1,
      Random.shuffle( ( 0 until e._2 ).toList )
    ) )

    /*val resourceVariantsAndCount:Map[Resource, (List[String], Int)] =
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
    ) )*/
  }

  /*private def getResourceImageVariants( r:Resource ):List[String] = {
    try {
      val dir = new java.io.File( s"public/images/${r.name.toLowerCase}" )
      if( dir.exists() && dir.isDirectory )
        dir.listFiles().toList.map( _.getName )
      else Nil
    } catch {
      case _:java.lang.SecurityException => Nil
    }
  }*/
}

case class GameSession( gameID:String,
                        hostID: String,
                        players:Map[String, Option[PlayerID]],
                        controller:Controller,
                        resourceImages:Map[Resource, List[Int]],
                        timestamp:Long = java.time.Instant.now().getEpochSecond,
                      ) {

  def update():GameSession = copy( timestamp = java.time.Instant.now().getEpochSecond )

  def addPlayer( sessionID:String ):GameSession =
    copy( players = players + ( sessionID -> None ), timestamp = java.time.Instant.now().getEpochSecond )

  def setPlayerID( sessionID:String, playerID:PlayerID ):GameSession =
    copy( players = players + ( sessionID -> Some( playerID ) ), timestamp = java.time.Instant.now().getEpochSecond )

  def removePlayer( sessionID:String ):GameSession =
    copy( players = players.removed( sessionID ), timestamp = java.time.Instant.now().getEpochSecond )
}