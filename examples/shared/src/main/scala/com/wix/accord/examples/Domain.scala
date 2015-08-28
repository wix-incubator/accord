package com.wix.accord.examples

import com.wix.accord.Validator
import com.wix.accord.dsl._

/**
 * Created by tomer on 12/24/14.
 */

trait Person {
  def name: String
  def surname: String
  def age: Int
}

object Person {
  val personValidator: Validator[ Person ] =
    validator[ Person ] { person =>
      person.name is notEmpty
      person.surname is notEmpty
      person.age is between( 0, 120 )
    }
}

case class Adult( name: String, surname: String, age: Int, contactInfo: String ) extends Person

case object Adult {
  implicit val adultValidator: Validator[ Adult ] =
    validator[ Adult ] { adult =>
      adult is valid( Person.personValidator )
      adult.age should be >= 18
      adult.contactInfo is notEmpty
    }

}

case class Minor( name: String, surname: String, age: Int, guardians: Set[ Adult ] ) extends Person

case object Minor {
  implicit val minorValidator: Validator[ Minor ] =
    validator[ Minor ] { minor =>
      minor is valid( Person.personValidator )
      minor.age should be < 18
      minor.guardians is notEmpty
      minor.guardians.each is valid
    }
}

case class Classroom( grade: Int, teacher: Adult, students: Set[ Minor ] )

case object Classroom {
  implicit val classroomValidator: Validator[ Classroom ] =
    validator[ Classroom ] { classroom =>
      classroom.grade is between( 1, 12 )
      classroom.teacher is valid
      classroom.students has size.between( 18, 25 )
      classroom.students.each is valid
    }
}


