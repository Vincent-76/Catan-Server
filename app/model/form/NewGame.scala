package model.form

import com.aimit.htwg.catan.model.{ FileIO, Placement }
import com.aimit.htwg.catan.{ CatanModule, CatanModuleImpl }
import model.InputForm
import play.api.data.Form
import play.api.data.Forms.{ mapping, set }

/**
 * @author Vincent76
 */
case class NewGame( module:CatanModuleImpl, fileIO:FileIO, availablePlacements:Set[Placement] )

object NewGame extends InputForm {
  val form:Form[NewGame] = Form( mapping(
    "module" -> CatanModule.mapping,
    "fileIO" -> FileIO.mapping,
    "placement" -> set[Placement]( Placement.mapping ).nonEmpty
  )( NewGame.apply )( NewGame.unapply ) )
}