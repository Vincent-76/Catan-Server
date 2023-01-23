package model

import com.aimit.htwg.catan.model.Card.ResourceCards
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

object InputForm {

  implicit class RichMapping[T]( m:Mapping[T] ) {
    def tryTransform[R]( f:T => R, t:R => T, default:T ):Mapping[Try[R]] = m.transform[Try[R]](
      i => Try{ f( i ) }.transform( Success.apply, _ => Failure( InvalidInput( i.toString ) ) ),
      _.map( t ).getOrElse( default )
    )
  }

  implicit class TryMapping[T]( m:Mapping[Try[T]] ) {
    def tryValidate( validate:T => Boolean = _ => true ):Mapping[Try[T]] = m.verifying( Constraint( ( v:Try[T] ) => v match {
      case Success( value ) =>
        if( validate( value ) )
          Valid
        else Invalid( InvalidInput( value.toString ).getMessage )
      case Failure( t ) => Invalid( t.getMessage )
    } ) )

    def untryTransform:Mapping[T] = m.transform( _.get, Success.apply )
  }


  val booleanMapping:Mapping[Boolean] = text
    .tryTransform[Boolean]( _.toBoolean, _.toString, "" )
    .tryValidate()
    .untryTransform

  def intMapping( validate:Int => Boolean ):Mapping[Int] = text
    .tryTransform[Int]( _.toInt, _.toString, "" )
    .tryValidate( validate )
    .untryTransform

  def componentMapping[T <: NamedComponentImpl]( component:NamedComponent[T] ):Mapping[T] = text
    .tryTransform[T]( component.of( _ ).get, _.name, "" )
    .tryValidate()
    .untryTransform

  def jsonMapping[T]( implicit reads:Reads[T], writes:Writes[T] ):Mapping[T] = text
    .tryTransform[T]( Json.parse( _ ).as[T], v => Json.stringify( Json.toJson( v ) ), "{}" )
    .tryValidate()
    .untryTransform



  def boolForm:Form[Boolean] =
    Form( booleanMapping )


  def positiveIntForm:Form[Int] = intForm( _ >= 0 )

  def intForm( validation:Int => Boolean = _ => true ):Form[Int] =
    Form( intMapping( validation ) )


  def componentForm[T <: NamedComponentImpl]( component:NamedComponent[T] ):Form[T] =
    Form( componentMapping( component ) )


  def jsonForm[T]( implicit reads:Reads[T], writes:Writes[T] ):Form[T] =
    Form( jsonMapping[T] )


  def doubleForm[T1, T2]( v1:(String, Mapping[T1]), v2:(String, Mapping[T2]) ):Form[(T1, T2)] = Form( mapping(
    v1,
    v2
  )( Tuple2.apply )( Tuple2.unapply ) )



  case class TestClass( give:ResourceCards, get:ResourceCards )
  def resourceCardsForm( v1:(String, Mapping[ResourceCards]), v2:(String, Mapping[ResourceCards]) ):Form[TestClass] = Form( mapping(
    v1,
    v2
  )( TestClass.apply )( TestClass.unapply ) )



  def tripleForm[T1, T2, T3]( v1:(String, Mapping[T1]), v2:(String, Mapping[T2]), v3:(String, Mapping[T3]) ):Form[(T1, T2, T3)] = Form( mapping(
    v1,
    v2,
    v3
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
