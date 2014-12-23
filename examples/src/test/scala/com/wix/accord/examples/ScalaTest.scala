package com.wix.accord.examples

import com.wix.accord.specs2.ResultMatchers
import org.specs2.matcher.Matchers
import org.specs2.mutable.Specification

/**
 * Created by tomer on 12/24/14.
 */
class ScalaTest extends Specification with Matchers with ResultMatchers {
  import com.wix.accord._
  import Domain._

  case class PersonTemplate( name: String, surname: String, age: Int ) extends Person

  "Person validator" should {
    "succeed on a valid person" in {
      val validPerson = PersonTemplate( name = "Grace", surname = "Hopper", age = 85 )
      validate( validPerson ) should succeed
    }

    "fail on an invalid person" in {
      val invalidPerson = PersonTemplate( "", "", -7 )
      validate( invalidPerson ) should failWith(
        "name" -> "must not be empty",
        "surname" -> "must not be empty",
        "age" -> "got -7, expected between 0 and 120"
      )
    }
  }

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
          violations = Set( group(
            description = "value",
            constraint = "is invalid",
            expectedViolations = "name" -> "must not be empty" ) ) ) )
    }
  }
}
