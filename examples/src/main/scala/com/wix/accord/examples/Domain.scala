package com.wix.accord.examples

/**
 * Created by tomer on 12/24/14.
 */

trait Person {
  def name: String
  def surname: String
  def age: Int
}

case class Adult( name: String, surname: String, age: Int, contactInfo: String ) extends Person

case class Minor( name: String, surname: String, age: Int, guardians: Set[ Adult ] ) extends Person

case class Classroom( grade: Int, teacher: Adult, students: Set[ Minor ] )
