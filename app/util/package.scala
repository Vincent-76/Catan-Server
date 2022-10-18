/**
 * @author Vincent76
 */
package object util {

  implicit class ConsoleString( val s:String ) {
    val consoleColors = List(
      Console.BLACK,
      Console.RED,
      Console.GREEN,
      Console.YELLOW,
      Console.BLUE,
      Console.MAGENTA,
      Console.CYAN,
      Console.WHITE,
      Console.BLUE_B,
      Console.RED_B,
      Console.GREEN_B,
      Console.YELLOW_B,
      Console.BLUE_B,
      Console.MAGENTA_B,
      Console.CYAN_B,
      Console.WHITE_B,
      Console.RESET,
      Console.BOLD,
      Console.UNDERLINED,
      Console.BLINK,
      Console.REVERSED,
      Console.INVISIBLE,
    )

    def filterColors:String = consoleColors.foldLeft( s )( ( s, c ) => s.replace( c, "" ) )
  }
}
