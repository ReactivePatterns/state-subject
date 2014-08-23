package com.reactivepatterns.snake

import swing._
import event.Key._
import com.reactivepatterns.snake.Screen._
import com.reactivepatterns.snake.World.Direction
import scala.swing.event.KeyPressed

sealed trait StateMessage
case class DirectionChange(to: WorldLocation) extends StateMessage
case class StateChange(snake: List[WorldLocation], apple: WorldLocation) extends StateMessage

class Board(interaction: => (Value) => Unit ) extends Panel {
  var doPaint: ((Graphics2D) => Unit) = (onGraphics) => {}
  preferredSize = new Dimension(Screen.scale(World.width), Screen.scale(World.height))
  focusable = true

  override def paintComponent(onGraphic: Graphics2D) {
    super.paintComponent(onGraphic)
    doPaint(onGraphic)
  }

  listenTo(keys)

  reactions += {
    case KeyPressed(source, key, modifiers, location) =>
      interaction(key)
  }

  def update(snake: List[ScreenLocation], apple: ScreenLocation) {
    def paintPoint(screenLocation: ScreenLocation, color: Color, onGraphics: Graphics2D) {
      onGraphics.setColor(color)
      onGraphics.fillRect(screenLocation.x, screenLocation.y, screenLocation.width, screenLocation.height)
    }

    doPaint = (onGraphics: Graphics2D) => {
      paintPoint(apple, new Color(210, 50, 90), onGraphics)
      snake.foreach {
        paintPoint(_, new Color(15, 160, 70), onGraphics)
      }
    }
    repaint()
  }
}

object Game extends SimpleSwingApplication {

  val directions = Map[Value, WorldLocation](
    Left -> Direction.Left,
    Right -> Direction.Right,
    Up -> Direction.Up,
    Down -> Direction.Down
  )

  val model = new GameState
  val view = new Board((key: Value) => model.tell(DirectionChange(directions(key))))

  model.stream().subscribe(m =>
    m match {
      case StateChange(snake, apple) =>
        view.update(toScreen(snake), toScreen(apple))
    })

  def top = new MainFrame {
    title = "Reactive Subjects"
    contents = new FlowPanel() {
      contents += view
    }
    pack()
  }
}



