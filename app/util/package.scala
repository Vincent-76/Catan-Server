import com.aimit.htwg.catan.model.GameField.Field
import com.aimit.htwg.catan.model.{ DesertArea, Game, GameField, Hex, Resource, ResourceArea, WaterArea }
import com.aimit.htwg.catan.util.{ RichAny, RichOption }

import scala.collection.mutable
import scala.util.Random

/**
 * @author Vincent76
 */
package object util {

  implicit class ConsoleString( val s:String ) {
    val consoleTextColors = Map(
      Console.BLACK -> "black",
      Console.RED -> "red",
      Console.GREEN -> "green",
      Console.YELLOW -> "darkkhaki",
      Console.BLUE -> "blue",
      Console.MAGENTA -> "magenta",
      Console.CYAN -> "cyan",
      Console.WHITE -> "white"
    )
    val consoleBackgroundColors = Map(
      Console.BLACK_B -> "black",
      Console.RED_B -> "red",
      Console.GREEN_B -> "green",
      Console.YELLOW_B -> "yellow",
      Console.BLUE_B -> "blue",
      Console.MAGENTA_B -> "magenta",
      Console.CYAN_B -> "cyan",
      Console.WHITE_B -> "white"
    )
    val consoleReset:String = Console.RESET
    val consoleOther = List(
      Console.RESET,
      Console.BOLD,
      Console.UNDERLINED,
      Console.BLINK,
      Console.REVERSED,
      Console.INVISIBLE
    )

    def filterColors:String = {
      val textFiltered = s.filterColors( consoleTextColors, "color" )
      val bgFiltered = textFiltered.filterColors( consoleBackgroundColors, "background-color" )
      val otherFiltered = bgFiltered.use( s => consoleOther.foldLeft( s )( ( s, c ) => {
        val res = s.replace( c, "" )
        res
      } ) )
      otherFiltered//.replace( "\n", "<br />" )
    }

    private def filterColors( colors:Map[String,String], attribute:String ):String = {
      val indices = colors.map( c => (c._1, c._2, s.indexOf( c._1 ) ) ).filter( c => c._3 >= 0 ).toSeq.sortBy( _._3 )
      if( indices.nonEmpty ) {
        val first = indices.head
        val nextTextColor = indices.lift( 1 )
        val nextReset = s.indexOf( consoleReset, first._3 + 1 )
        val end = if( nextTextColor.isDefined && ( nextReset < 0 || nextTextColor.get._3 < nextReset ) ) {
          nextTextColor.get._3
        } else if( nextReset > 0 ) {
          nextReset
        } else {
          s.length
        }
        val s1 = s.patch( end, "</span>", 0 )
        val s2 = s1.patch( first._3, s"<span style=\"$attribute:${first._2};white-space:pre\">", first._1.length )
        s2.filterColors( colors, attribute )
      } else s
    }
  }



  implicit class RichIterable[A]( iterable:Iterable[A] ) {
    def mapWhere[C]( p: A => Boolean, f: A => C ):Iterable[C] = {
      val b = iterable.iterableFactory.newBuilder[C]
      val it = iterable.iterator
      while( it.hasNext ) {
        val e = it.next()
        if( p( e ) )
          b += f( e )
      }
      b.result()
    }
  }


  def getImagePath( game:Game, resourceImages:Map[Resource, List[String]], hex:Hex, resourceCounter:scala.collection.mutable.Map[Resource, Int] ):String = hex.area match {
    case _:DesertArea => "desert.png"
    case WaterArea( None ) => "water.png"
    case WaterArea( Some( port ) ) =>
      val rotNr = GameField.adjacentOffset.indices.find( i => game.gameField.adjacentEdge( hex, i ) match {
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
      resourceImages.getOrElse( area.resource, List.empty ).lift( i ).useOrElse(
        s => s"${area.resource.title.toLowerCase}/$s",
        "water.png" // TODO fallback image
      )
  }
}
