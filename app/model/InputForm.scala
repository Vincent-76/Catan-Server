package model

import com.aimit.htwg.catan.model.{ NamedComponent, NamedComponentImpl }
import model.form.NewGame.NamedComponentMapping
import play.api.data.Forms.{ mapping, number, text }
import play.api.data.validation.{ Constraint, Invalid, Valid }
import play.api.data.{ Form, Mapping }
import play.api.libs.json.{ Json, Reads, Writes }

import scala.util.{ Failure, Success, Try }

/**
 * @author Vincent76
 */

case class FormData[T]( value:T )

object InputForm {

  private def tryTransform[T, R]( t:T, f:T => R ):Try[R] = Try{
    f( t )
  }.transform( Success.apply, _ => Failure( InvalidInput( t.toString ) ) )

  val booleanMapping:Mapping[Boolean] = text
    .transform[Try[Boolean]](
      s => tryTransform[String, Boolean]( s, _.toBoolean ),
      _.map( _.toString ).getOrElse( "" )
    )
    .verifying( Constraint[Try[Boolean]] {
      case Success( _ ) => Valid
      case Failure( t ) => Invalid( t.getMessage )
    } )
    .transform[Boolean]( _.get, Success.apply )

  def intMapping( validation:Int => Boolean ):Mapping[Int] = text
    .transform[Try[Int]](
      s => tryTransform[String, Int]( s, _.toInt ),
      _.map( _.toString ).getOrElse( "" )
    )
    .verifying( Constraint[Try[Int]] {
      case Success( i:Int ) =>
        if( validation( i ) )
          Valid
        else Invalid( InvalidInput( i.toString ).getMessage )
      case Failure( t ) => Invalid( t.getMessage )
    } )
    .transform[Int]( _.get, Success.apply )

  def jsonMapping[T]( implicit reads:Reads[T], writes:Writes[T] ):Mapping[T] = text
    .transform[Try[T]](
      s => tryTransform[String, T]( s, s => Json.parse( s ).as[T] ),
      _.map( e => Json.stringify( Json.toJson( e ) ) ).getOrElse( "{}" )
    )
    .verifying( Constraint[Try[T]] {
      case Success( _ ) => Valid
      case Failure( t ) => Invalid( t.getMessage )
    } )
    .transform[T]( _.get, Success.apply )



  def boolForm:Form[FormData[Boolean]] = Form( mapping(
    "value" -> booleanMapping
  )( FormData.apply )( FormData.unapply ) )


  def positiveIntForm:Form[FormData[Int]] = intForm( _ >= 0 )

  def intForm( validation:Int => Boolean = _ => true ):Form[FormData[Int]] = Form( mapping(
    "value" -> intMapping( validation )
  )( FormData.apply )( FormData.unapply ) )


  def singleForm[T <: NamedComponentImpl]( component:NamedComponent[T] ):Form[FormData[T]] = Form( mapping(
    "value" -> component.mapping
  )( FormData.apply )( FormData.unapply ) )


  def jsonForm[T]( implicit reads:Reads[T], writes:Writes[T] ):Form[FormData[T]] = Form( mapping(
    "value" -> jsonMapping[T]
  )( FormData.apply )( FormData.unapply ) )


  def doubleForm[T1, T2]( m1:Mapping[T1], m2:Mapping[T2] ):Form[(T1, T2)] = Form( mapping(
    "_1" -> m1,
    "_2" -> m2
  )( Tuple2.apply )( Tuple2.unapply ) )


  def tripleForm[T1, T2, T3]( m1:Mapping[T1], m2:Mapping[T2], m3:Mapping[T3] ):Form[(T1, T2, T3)] = Form( mapping(
    "_1" -> m1,
    "_2" -> m2,
    "_3" -> m3
  )( Tuple3.apply )( Tuple3.unapply ) )
}



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
