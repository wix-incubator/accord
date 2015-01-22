package com.wix.accord.examples

import com.wix.accord.specs2.ResultMatchers
import org.specs2.matcher.Matchers
import org.specs2.mutable.Specification

import scala.util.Random
import com.wix.accord.simple

/**
 * Created by tomer on 12/24/14.
 */
class Specs2 extends Specification with Matchers with ResultMatchers[ simple.type ] {
  import simple._

  val validAdult = Adult( name = "Grace", surname = "Hopper", age = 85, contactInfo = "Arlington National Cemetery" )

  "Adult validator" should {
    "succeed on a valid adult" in {
      validate( validAdult ) should succeed
    }
    "fail on an invalid person" in {
      val invalidAdult = validAdult.copy( name = "" )
      validate( invalidAdult ) should fail
    }
    "fail on a minor" in {
      val invalidAdult = validAdult.copy( age = 15 )
      validate( invalidAdult ) should failWith( "age" -> "got 15, expected 18 or more" )
    }
    "fail on an adult with missing contact info" in {
      val invalidAdult = validAdult.copy( contactInfo = "" )
      validate( invalidAdult ) should failWith( "contactInfo" -> "must not be empty" )
    }
  }

  "Minor validator" should {
    val validMinor = Minor(
      name = "Blaise",
      surname = "Pascal",
      age = 16,
      guardians = Set( Adult( name = "Étienne", surname = "Pascal", age = 63, contactInfo = "Paris, France" ) ) )

    "succeed on a valid minor" in {
      validate( validMinor ) should succeed
    }

    "fail on an invalid person" in {
      val invalidMinor = validMinor.copy( name = "" )
      validate( invalidMinor ) should fail
    }

    "fail on an adult" in {
      val invalidMinor = validMinor.copy( age = 25 )
      validate( invalidMinor ) should failWith( "age" -> "got 25, expected less than 18" )
    }

    "fail on a minor with no guardians" in {
      val invalidMinor = validMinor.copy( guardians = Set.empty )
      validate( invalidMinor ) should failWith( "guardians" -> "must not be empty" )
    }

    "fail on a minor with an invalid guardian" in {
      val invalidMinor = validMinor.copy( guardians = Set( validAdult.copy( name = "" ) ) )
      validate( invalidMinor ) should failWith(
        GroupViolationMatcher(
          description = "guardians",
          violations = Set(
            GroupViolationMatcher(
              description = "value",
              constraint = "is invalid" ) ) ) )
    }
  }

  "Classroom validator" should {
    val minorPool: Iterator[ Minor ] =
      Iterator.continually {
        Minor( name = Random.nextString( 10 ), surname = Random.nextString( 10 ), age = Random.nextInt( 18 ), guardians = Set( validAdult ) )
      }
    val validClassroom = new Classroom( grade = 3, teacher = validAdult, students = minorPool.take( 20 ).toSet )

    "succeed on a valid classroom" in {
      validate( validClassroom ) should succeed
    }

    "fail on a classroom with an invalid grade" in {
      val invalidClassroom = validClassroom.copy( grade = -3 )
      validate( invalidClassroom ) should failWith( "grade" -> "got -3, expected between 1 and 12" )
    }

    "fail on a classroom with an invalid teacher" in {
      val invalidClassroom = validClassroom.copy( teacher = validAdult.copy( age = -5 ) )
      validate( invalidClassroom ) should failWith( GroupViolationMatcher( description = "teacher", constraint = "is invalid" ) )
    }

    "fail on a classroom with an invalid student" in {
      val invalidStudent = minorPool.next().copy( name = "" )
      val invalidClassroom = validClassroom.copy( students = validClassroom.students + invalidStudent )
      validate( invalidClassroom ) should failWith(
        GroupViolationMatcher(
          description = "students",
          violations = Set(
            GroupViolationMatcher(
              description = "value",
              constraint = "is invalid" ) ) ) )
    }

    "fail on an empty classroom" in {
      val invalidClassroom = validClassroom.copy( students = Set.empty )
      validate( invalidClassroom ) should failWith( "students" -> "has size 0, expected between 18 and 25" )
    }

    "fail on an overcrowded classroom" in {
      val invalidClassroom = validClassroom.copy( students = minorPool.take( 100 ).toSet )
      validate( invalidClassroom ) should failWith( "students" -> "has size 100, expected between 18 and 25" )
    }
  }
}
