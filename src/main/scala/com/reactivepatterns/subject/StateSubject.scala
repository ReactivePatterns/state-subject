package com.reactivepatterns.subject

import rx.lang.scala.subjects.{ReplaySubject, BehaviorSubject, PublishSubject}
import rx.lang.scala.{Observable, Subject}

/**
 * Created by Constantin on 3/29/14.
 */

trait ReactiveState[M, S] {
  def tell(m: M)
  def stream(): Observable[S]
}

class StateSubject[M, S](init: S, behavior: PartialFunction[(S, M), S],
                            private[this] val messages: Subject[M]) extends ReactiveState[M, S] {

  private[this] var current: S = init

  private[this] val state: Observable[S] =
    messages.scan(init)((s, m) => {
      val update = behavior(s, m)
      current = update
      update
    })

  //start
  state.subscribe({observer =>})

  def this(init: S, initMessage: M, behavior: PartialFunction[(S, M), S]) =
    this(init, behavior, BehaviorSubject[M](initMessage))

  def this(init: S, behavior: PartialFunction[(S, M), S], sources: Observable[M]*) = {
    this(init, behavior, Subject[M]())
    sources.foreach {
      source =>
        source.subscribe(
          (m: M) => tell(m),
          (t: Throwable) => Console.println(t.printStackTrace())
        )
    }
  }

//  def this(behavior: /*Partial*/Function[M, S],
//                  source: StateSubject[M, M]) = {
//    this(behavior(source.get()), new PartialFunction[(S, M), S] {
//      override def isDefinedAt(x: (S, M)): Boolean = true
//      override def apply(v: (S, M)): S = behavior(v._2)
//    }, source.stream())
//  }



  def tell(m: M) = {
    messages.onNext(m)
  }

  def stream(): Observable[S] = {
    Observable.items(current) ++ state.drop(1)
    //state
  }

  def apply(): S = get

  def get(): S = current

  def latest: Observable[S] = state.sample(Observable.items(1)).take(1)
}

class Updater[S] extends PartialFunction[(S, S), S] {
  override def isDefinedAt(x: (S, S)): Boolean = true
  override def apply(current_updated: (S, S)): S = current_updated._2
}

object StateSubject {

  def apply[M, S](init: S, behavior: PartialFunction[(S, M), S]): StateSubject[M, S] = {
    new StateSubject[M, S](init, behavior)
  }

  def apply[T, U, S](behavior: /*Partial*/Function[(T, U), S],
                     source1: StateSubject[T, T], source2: StateSubject[U, U]): StateSubject[(T, U), S] = {
    val b: PartialFunction[(S, (T, U)), S] = {
      case (s, (t, u)) => behavior(t, u)
    }
    new StateSubject[(T, U), S](behavior(source1(), source2()), b, source1.stream.combineLatest(source2.stream))
  }

  def apply[T, S](behavior: Function[T, S],
                     source: StateSubject[_, T]): StateSubject[T, S] = {
    val b: PartialFunction[(S, T), S] = {
      case (s, t) => behavior(t)
    }
    new StateSubject[T, S](behavior(source()), b, source.stream())
  }

  def apply[T, U, S](init: S, behavior: PartialFunction[(S, T, U), S],
                        source1: Observable[T], source2: Observable[U]): StateSubject[(T, U), S] = {
    val b: PartialFunction[(S, (T, U)), S] = {
      case (s, (t, u)) => behavior(s, t, u)
    }
    new StateSubject[(T, U), S](init, b, source1.combineLatest(source2))
  }

  def apply[T, U, V, S](init: S, behavior: PartialFunction[(S, ((T, U), V)), S],
    source1: Observable[T], source2: Observable[U], source3: Observable[V]): StateSubject[((T, U), V), S] = {
    new StateSubject[((T, U), V), S](init, behavior, source1.combineLatest(source2).combineLatest(source3))
  }
}

class ReactiveVar[S](init: S) extends StateSubject[S, S](init/*, init*/, new Updater[S]()) {

  def set(updated: S) = {
    tell(updated)
    this
  }
}

object ReactiveVar {
  def apply[S](init: S): ReactiveVar[S] = {
    new ReactiveVar(init)
  }

//  def apply[S, T](init: S, updater: PartialFunction[(S, T), S], dep: ReactiveVar[T]): StateSubject[T, S] = {
//    new StateSubject(init, updater, dep.stream())
//  }
}
