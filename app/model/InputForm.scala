package model

import com.aimit.htwg.catan.model.{ NamedComponent, NamedComponentImpl }
import play.api.data.Forms.text
import play.api.data.Mapping

/**
 * @author Vincent76
 */

abstract class InputForm {
  implicit class NamedComponentMapping[I <: NamedComponentImpl]( component:NamedComponent[I] ) {
    def mapping:Mapping[I] = text
      .verifying( ValidationError.Invalid.name, s => component.hasImpl( s ) )
      .transform[I]( component.of( _ ).get, _.name )
  }

  implicit class IterableMapping[E, I[_] <: Iterable[_]]( mapping:Mapping[I[E]] ) {
    def nonEmpty:Mapping[I[E]] = mapping.verifying( ValidationError.Empty.name, _.nonEmpty )
  }

  def nonEmptyText:Mapping[String] = text.verifying( ValidationError.Empty.name, _.nonEmpty )
}
