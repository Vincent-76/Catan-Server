package model

import com.aimit.htwg.catan.controller.Controller

/**
 * @author Vincent76
 */
case class SessionController( controller:Controller, timestamp:Long = java.time.Instant.now().getEpochSecond ) {

  def update():SessionController = copy( controller )
}
