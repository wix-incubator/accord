/*
  Copyright 2013 Tomer Gabel

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

package com.tomergabel.accord

import org.scalatest.{Matchers, WordSpec}

class ValidationTransformTests extends WordSpec with Matchers with ResultMatchers {
  import PrimitiveSchema._
  val personWithNoName = Person( "", "" )
  val personWithNoFirstName = Person( "", "last" )
  val personWithNoLastName = Person( "first", "" )
  val legitPerson1 = Person( "first", "person" )
  val legitPerson2 = Person( "second", "person" )
  val legitPerson3 = Person( "third", "dude" )
  val classWithInvalidTeacher = Classroom( personWithNoName, Seq( legitPerson1, legitPerson2, legitPerson3 ) )
  val classWithNoStudents = Classroom( legitPerson1, Seq.empty )
  val classWithInvalidStudent = Classroom( legitPerson1, Seq( legitPerson2, personWithNoLastName ) )

  "personValidator" should {
    "fail a person with no first name" in {
      val result = validate( personWithNoFirstName )
      result should failWith( "firstName must not be empty" )
    }
    "fail a person with no last name" in {
      val result = validate( personWithNoLastName )
      result should failWith( "lastName must not be empty" )
    }
    "pass a person with a full name" in {
      val result = validate( legitPerson1 )
      result should be( aSuccess )
    }
  }

  "classroomValidator" should {
    "fail a classroom with no students" in {
      val result = validate( classWithNoStudents )
      result should failWith( "students has size 0, expected more than 0" )
    }
    "fail a classroom with an invalid teacher" in {
      val result = validate( classWithInvalidTeacher )
      result should failWith( "teacher firstName must not be empty", "teacher lastName must not be empty" )
    }
    "fail a classroom with an invalid student" in {
      val result = validate( classWithInvalidStudent )
      result should failWith( "students lastName must not be empty" )
    }
  }
}
