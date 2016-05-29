package com.wix.accord.tests.repro

case class Bar(minValue: Double, maxValue: Double)
case object Bar {
  import com.wix.accord.Validator
  import com.wix.accord.dsl._

  implicit val defaultBarValidator: Validator[Bar] = validator[Bar] { b =>
    b.minValue should be >= 0.0
    b.maxValue should be <= 10.0
  }

  lazy val specialBarValidator: Validator[Bar] = validator[Bar] { b =>
    b.minValue should be >= 0.5
    b.maxValue should be <= 1.0
  }
}

case class Foo(bar: Bar)
case object Foo {
  import com.wix.accord.Validator
  import com.wix.accord.dsl._
  import Bar.specialBarValidator

  implicit val fooValidator: Validator[Foo] = validator[Foo] { f =>
    f.bar is specialBarValidator
  }
}

object Issue66 extends App {

  import com.wix.accord._
  println(
    validate(Foo(Bar(-0.4, 100.0)))
  )
}