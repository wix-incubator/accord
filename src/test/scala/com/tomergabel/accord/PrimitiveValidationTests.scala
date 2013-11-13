package com.tomergabel.util.validation

import org.scalatest.{Matchers, WordSpec}
import org.scalatest.matchers._

/**
* Created by tomer on 8/7/13.
*/

object PrimitiveValidationTests {
  import builder._

  case class Person( firstName: String, lastName: String )
  case class Classroom( teacher: Person, students: Seq[ Person ] )

  implicit val personValidator = validator[ Person ] { p =>
    p.firstName is notEmpty
    p.lastName is notEmpty
  }

  implicit val classValidator = validator[ Classroom ] { c =>
    c.teacher is valid
    c.students.each is valid
    c.students have size > 0
  }
}

import PrimitiveValidationTests._
class PrimitiveValidationTests extends WordSpec with Matchers {

  val personWithNoName = Person( "", "" )
  val personWithNoFirstName = Person( "", "last" )
  val personWithNoLastName = Person( "first", "" )
  val legitPerson1 = Person( "first", "person" )
  val legitPerson2 = Person( "second", "person" )
  val legitPerson3 = Person( "third", "dude" )
  val classWithInvalidTeacher = Classroom( personWithNoName, Seq( legitPerson1, legitPerson2, legitPerson3 ) )
  val classWithNoStudents = Classroom( legitPerson1, Seq.empty )
  val classWithInvalidStudent = Classroom( legitPerson1, Seq( legitPerson2, personWithNoLastName ) )

  def failWith( expectedViolations: String* ) = new Matcher[ Result ] {
    def apply( left: Result ): MatchResult =
      left match {
        case Success => MatchResult( matches = false, "Validation was successful", "Validation was not successful" )
        case Failure( vlist ) =>
          val violations = vlist.map { _.constraint }.toSet
          val remainder = expectedViolations.toSet -- violations
          val unexpected = violations.toSet -- expectedViolations.toSet
          MatchResult( matches = remainder.isEmpty && unexpected.isEmpty,
            "Validation failed with unexpected violations!\nExpected violations that weren't found:\n"
              + remainder.mkString( "\t", "\n\t", "\n" )
              + "Unexpected violations:\n" + unexpected.mkString( "\t", "\n\t", "\n" ),
            // How to negate?
            "Validation failed with unexpected violations!\nExpected violations that weren't found:\n"
              + remainder.mkString( "\t", "\n\t", "\n" )
              + "Unexpected violations:\n" + unexpected.mkString( "\t", "\n\t", "\n" )
          )
      }
  }

  val aFailure = new BeMatcher[ Result ] {
    def apply( left: Result ) = MatchResult( left.isInstanceOf[ Failure ], "not a failure", "is a failure" )
  }
  val aSuccess = new BeMatcher[ Result ] {
    def apply( left: Result ) = MatchResult( left == Success, "not a success", "is a success" )
  }

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
