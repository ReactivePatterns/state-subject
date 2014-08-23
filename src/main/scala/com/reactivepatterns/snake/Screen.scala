package com.reactivepatterns.snake

case class ScreenLocation(x: Int, y:Int, width: Int, height: Int)


object Screen {
  val factor = 10

  def scale(length: Int): Int = length * factor

  def toScreen(locations: List[WorldLocation]): List[ScreenLocation] = locations.map(toScreen(_))

  def toScreen(location: WorldLocation): ScreenLocation = {
    ScreenLocation(location.x * factor, location.y * factor, factor, factor)
  }

}