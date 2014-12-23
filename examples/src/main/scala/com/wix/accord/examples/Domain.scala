package com.wix.accord.examples

/**
 * Created by tomer on 12/24/14.
 */
trait BaseDomain {
  import com.wix.accord._
  import dsl._

  trait Person {
    def name: String
    def surname: String
    def age: Int
  }
  object Person {
    implicit val personValidator: Validator[ Person ] =
      validator[ Person ] { person =>
        person.name is notEmpty
        person.surname is notEmpty
        person.age is between( 0, 120 )
      }
  }
}

object Domain extends BaseDomain {

  case class Minor( name: String, surname: String, age: Int, guardians: Set[ Adult ] ) extends Person
  case class Adult( name: String, surname: String, age: Int, contactInfo: String ) extends Person
  case class Classroom( grade: Int, teacher: Adult, students: Set[ Minor ] )

  import com.wix.accord._
  import dsl._

  implicit val adultValidator: Validator[ Adult ] =
    validator[ Adult ] { adult =>
      adult is valid[ Person ]
      adult.age should be >= 18
      adult.contactInfo is notEmpty
    }

  implicit val minorValidator: Validator[ Minor ] =
    validator[ Minor ] { minor =>
      minor is valid[ Person ]
      minor.age should be < 18
      minor.guardians is notEmpty
      minor.guardians.each is valid( adultValidator )
    }

  implicit val classroomValidator: Validator[ Classroom ] =
    validator[ Classroom ] { classroom =>
      classroom.grade is between( 1, 12 )
      classroom.teacher is valid
      classroom.students has size.between( 18, 25 )
    }
}
