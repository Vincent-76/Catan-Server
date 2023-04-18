package model.form

import play.api.data.Form
import play.api.data.Forms.{ mapping, text }

/**
 * @author Vincent76
 */

case class TUIInput( input:String )

object TUIInput {
  val form:Form[TUIInput] = Form( mapping(
    "form_input" -> text
  )( TUIInput.apply )( TUIInput.unapply ) )
}