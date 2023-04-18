package controllers

import play.api.data.{ Form, FormError }
import play.api.i18n.I18nSupport
import play.api.mvc.{ Action, ActionBuilder, AnyContent, BaseController, BodyParser, ControllerComponents, Request, RequestHeader, Result, Results }
import play.core.Execution
import play.libs.F
import views.html.helper.form

import scala.concurrent.ExecutionContext

/**
 * @author Vincent76
 */
class CatanBaseController( val controllerComponents:ControllerComponents )( implicit executionContext:ExecutionContext ) extends BaseController with I18nSupport {


  def formAction[F]( form:Form[F],
                     onErrors:(RequestHeader, Map[String, List[String]]) => Result
                   ):ActionBuilder[Request, F] = Action( BodyParser { implicit requestHeader =>
    val parser = parse.anyContent( None )
    val binding = parse.formBinding( parse.DefaultMaxTextLength )
    val res = parser( requestHeader ).map( _.flatMap( body =>
      form
        .bindFromRequest()( Request[AnyContent]( requestHeader, body ), binding )
        .fold(
          errorForm => Left( onErrors( requestHeader, errorForm.errors.map( e => (e.key, e.messages.toList) ).toMap ) ),
          a => Right( a )
        )
    )
    )( Execution.trampoline )
    res
  } )

}
