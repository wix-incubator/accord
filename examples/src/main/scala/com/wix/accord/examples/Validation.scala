package com.wix.accord.examples

import com.wix.accord.Validator
import com.wix.accord.dsl._

/**
 * Created by tomer on 12/24/14.
 */
object Validation {
  private val personValidator: Validator[ Person ] =
    validator[ Person ] { person =>
      person.name is notEmpty
      person.surname is notEmpty
      person.age is between( 0, 120 )
    }

  implicit val adultValidator: Validator[ Adult ] =
    validator[ Adult ] { adult =>
      adult is valid( personValidator )
      adult.age should be >= 18
      adult.contactInfo is notEmpty
    }

  implicit val minorValidator: Validator[ Minor ] =
    validator[ Minor ] { minor =>
      minor is valid( personValidator )
      minor.age should be < 18
      minor.guardians is notEmpty
      minor.guardians.each is valid
    }

  implicit val classroomValidator: Validator[ Classroom ] =
    validator[ Classroom ] { classroom =>
      classroom.grade is between( 1, 12 )
      classroom.teacher is valid
      classroom.students has size.between( 18, 25 )
      classroom.students.each is valid
    }
}
