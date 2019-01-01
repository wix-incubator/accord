/*
  Copyright 2013-2019 Wix.com

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
 */

package com.wix.accord.examples

import com.wix.accord.Validator
import com.wix.accord.dsl._

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


