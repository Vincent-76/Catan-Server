package controllers

import com.aimit.htwg.catan.CatanModule
import com.aimit.htwg.catan.controller.Controller
import com.aimit.htwg.catan.view.tui.TUI

import javax.inject._
import play.api._
import play.api.mvc._
import com.google.inject.{ Guice, Injector }

import java.io.FileInputStream
import scala.collection.mutable
import scala.collection.mutable.Map


/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class HomeController @Inject()( val controllerComponents:ControllerComponents ) extends BaseController {


  /**
   * Create an Action to render an HTML page.
   *
   * The configuration in the `routes` file means that this method
   * will be called when the application receives a `GET` request with
   * a path of `/`.
   */
  def index( ):Action[AnyContent] = Action { implicit request:Request[AnyContent] =>
    Ok( views.html.index() )
  }


  def rules( ):Action[AnyContent] = Action { implicit request:Request[AnyContent] =>
    Ok( views.html.index() )
  }

}