package model

import play.api.data.Form
import play.api.data.Forms.{ mapping, text }

/**
 * @author Vincent76
 */

case class InputForm( input:String )

object InputForm {
  val form:Form[InputForm] = Form( mapping(
    "input" -> text
  )( InputForm.apply )( InputForm.unapply ) )
}