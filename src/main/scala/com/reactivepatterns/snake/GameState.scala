package com.reactivepatterns.snake

import com.reactivepatterns.snake.World._
import rx.lang.scala.Observable
import scala.concurrent.duration.Duration
import java.util.concurrent.TimeUnit
import com.reactivepatterns.subject.{ReactiveState, StateSubject}

class GameState extends ReactiveState[DirectionChange, StateChange] {

  implicit def StateSubjectToObservable[M, S](rs : StateSubject[M, S]): Observable[S] = rs.stream()

  case class Snake(body: List[WorldLocation], direction: WorldLocation)

  sealed trait Event
  case class Turn(direction: WorldLocation) extends Event
  case class Eat() extends Event
  case class Advance() extends Event

  val snakeBehavior: PartialFunction[(Snake, Event), Snake] = {
    case (snake: Snake, Advance()) => Snake((snake.body.head + snake.direction) ::
      snake.body.take(snake.body.size - 1), snake.direction)
    case (snake: Snake, Eat()) => Snake((snake.body.head + snake.direction) ::
      snake.body, snake.direction)
    case (snake: Snake, Turn(direction)) => Snake(snake.body, direction)
  }

  val appleBehavior: PartialFunction[(WorldLocation, Snake), WorldLocation] = {
    case (loc: WorldLocation, snake: Snake) => snake.body.head match {
      case head if (head == loc) => {
        snakeSubject.tell(Eat())
        randomLocation()
      }
      case _ => loc
    }
  }

  val tick = Observable.interval(Duration(150, TimeUnit.MILLISECONDS))

  val snakeSubject: StateSubject[Event, Snake] =
    new StateSubject[Event, Snake](Snake(List(origin), Direction.Right),
      snakeBehavior, tick.map(_ => Advance()))

  val appleSubject =
    new StateSubject[Snake, WorldLocation](randomLocation(), appleBehavior, snakeSubject)

//  tick.subscribe(
//    (_ => snakeSubject.tell(Advance())),
//    (t: Throwable) => println("tick error : " + t),
//    () => {}
//  )



  override def tell(m: DirectionChange): Unit = {
    m match {
      case DirectionChange(to) => snakeSubject.tell(Turn(to))
    }
  }

  override def stream(): Observable[StateChange] = {
    snakeSubject.combineLatest(appleSubject).map(
      pair => {
        StateChange(pair._1.body, pair._2)
      })
  }
}
