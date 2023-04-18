package model.form

import com.aimit.htwg.catan.model.PlayerColor
import model.{ InputForm, ValidationError }
import play.api.data.Form
import play.api.data.Forms.mapping

/**
 * @author Vincent76
 */

case class AddPlayer( name:String, color:PlayerColor )

object AddPlayer extends InputForm {
  val form:Form[AddPlayer] = Form( mapping(
    "name" -> nonEmptyText.verifying( ValidationError.TooLong.name, _.length <= 10 ),
    "color" -> PlayerColor.mapping
  )( AddPlayer.apply )( AddPlayer.unapply ) )
}
