package com.reactivepatterns.subject

import rx.lang.scala.Observable


/**
 * Created by *stan* on 4/2/14.
 */
object SubjectTests extends App {
  implicit def StateSubjectToObservable[M, S](rs : StateSubject[M, S]): Observable[S] = rs.stream()

  val a = ReactiveVar(1)
  val b = ReactiveVar(2)

  val cFormula: /*Partial*/Function[(Int, Int), Int] =  {
    case (a: Int, b: Int) => a + b
  }

  val cFormula1: Function[(Int, Int), Int] =  {pair: (Int, Int) => pair._1 + pair._2}

  val c: StateSubject[(Int, Int), Int] = StateSubject({pair: (Int, Int) => pair._1 + pair._2}, a, b)
  //val c = StateSubject(cFormula, a, b)

  val d = StateSubject({(c: Int) => c * c}, c)

  println("d snapshot: "  + d.get())

  d.subscribe((v) => println("d: " + v))

  a.set(5)
  b.set(4)

  println("d snapshot: "  + d.get())

//  var rs = ReactiveVar(5)
//  rs.set(10)
//  rs.set(20)
//  Console.println("GETTER " + rs.get())
//
//  rs.subscribe((v) => Console.println("LATEST " + v))
//
//  var rs1 = ReactiveVar(15)
//
//  var rs2 = ReactiveVar("abc")
//
////  new StateSubject(1000, {case (c: Int, dep: Int) => c + dep}: PartialFunction[(Int, Int), Int], rs, rs1)
////    .subscribe((v) => Console.println("DEP " + v))
//
//  val one: PartialFunction[(Int, Int), Int] =  {
//    case (c: Int, dep: Int) => {
//      println(c, dep)
//      c + dep
//    }
//  }
//
//  val two: PartialFunction[(Int, Int, Int), Int] =  {
//    case (c: Int, dep1: Int, dep2: Int) => {
//      //println(c, dep1, dep2)
//      c + dep1 + dep2
//    }
//  }
//
//  val two2: PartialFunction[(Int, Int, String), Int] =  {
//    case (c: Int, dep1: Int, dep2: String) => {
//      println(c, dep1, dep2)
//      c + dep1 + dep2.length
//    }
//  }
////
////  new StateSubject(1000, one, rs)
////    .subscribe((v) => Console.println("DEP ONE " + v))
////
////  StateSubject(1000, two, rs, rs1)
////    .subscribe((v) => Console.println("DEP TWO " + v))
//
//  StateSubject(1000, two2, rs, rs2)
//    .subscribe((v) => Console.println("DEP TWO2 " + v))
//
//
//  rs1.set(55);
//
//  rs.set(30)
//  rs1.set(77);
//
//  rs.subscribe((v) => Console.println("RS " + v))
//
//  rs.set(40)
//  rs.set(50)
//
//  Console.println("GETTER " + rs.get())
//
//  rs.set(100)
}
