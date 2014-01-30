/*
  Copyright 2013 Wix.com

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

package com.wix.accord.tests.dsl

import org.scalatest.{Matchers, WordSpec}
import com.wix.accord._
import com.wix.accord.scalatest.ResultMatchers

class ValidationTransformTests extends WordSpec with Matchers with ResultMatchers {
  // TODO reconsider test and potentially remove
//  import PrimitiveSchema._
//  val personWithNoName = Person( "", "" )
//  val personWithNoFirstName = Person( "", "last" )
//  val personWithNoLastName = Person( "first", "" )
//  val legitPerson1 = Person( "first", "person" )
//  val legitPerson2 = Person( "second", "person" )
//  val legitPerson3 = Person( "third", "dude" )
//  val classWithInvalidTeacher = Classroom( personWithNoName, Seq( legitPerson1, legitPerson2, legitPerson3 ) )
//  val classWithNoStudents = Classroom( legitPerson1, Seq.empty )
//  val classWithInvalidStudent = Classroom( legitPerson1, Seq( legitPerson2, personWithNoLastName ) )
//
//  "personValidator" should {
//    "fail a person with no first name" in {
//      val result = validate( personWithNoFirstName )
//      result should failWith( "firstName" -> "must not be empty" )
//    }
//    "fail a person with no last name" in {
//      val result = validate( personWithNoLastName )
//      result should failWith( "lastName" -> "must not be empty" )
//    }
//    "pass a person with a full name" in {
//      val result = validate( legitPerson1 )
//      result should be( aSuccess )
//    }
//  }
//
//  "classroomValidator" should {
//    "fail a classroom with no students" in {
//      val result = validate( classWithNoStudents )
//      result should failWith( "students" -> "has size 0, expected more than 0" )
//    }
//    "fail a classroom with an invalid teacher" in {
//      val result = validate( classWithInvalidTeacher )
//      result should failWith( group( "teacher", "is invalid",
//        "firstName" -> "must not be empty",
//        "lastName" -> "must not be empty"
//      ) )
//    }
//    "fail a classroom with an invalid student" in {
//      val result = validate( classWithInvalidStudent )
//      result should failWith( group( "students", "is invalid",
//        "lastName" -> "must not be empty" ) )
//    }
//  }

  "Validator description" should {
    import ValidationTransformTests._

    "be generated for a fully-qualified field selector" in {
      validate( FlatTest( null ) )( implicitlyDescribedNamedValidator ) should failWith( "field" -> "is a null" )
    }
    "be generated for an anonymously-qualified field selector" in {
      validate( FlatTest( null ) )( implicitlyDescribedAnonymousValidator ) should failWith( "field" -> "is a null" )
    }
    "be generated for an anonymous value reference" in {
      validate( null )( implicitlyDescribedValueValidator ) should failWith( "value" -> "is a null" )
    }
    "be generated for a fully-qualified selector with multiple indirections" in {
      val obj = CompositeTest( FlatTest( null ) )
      validate( obj )( namedIndirectValidator ) should failWith( "member.field" -> "is a null" )
    }
    "be generated for an anonymously-qualified selector with multiple indirections" in {
      val obj = CompositeTest( FlatTest( null ) )
      validate( obj )( anonymousIndirectValidator ) should failWith( "member.field" -> "is a null" )
    }
    "be propagated for an explicitly-described expression" in {
      validate( FlatTest( null ) )( explicitlyDescribedValidator ) should failWith( "described" -> "is a null" )
    }
    "be propagated for a composite validator" in {
      val obj = CompositeTest( FlatTest( null ) )
      validate( obj )( compositeValidator ) should failWith( group( "member", "is invalid", "field" -> "is a null" ) )
    }
//    "be propagated for an adapted validator" in {
//      validate( FlatTest( null ) )( adaptedValidator ) should failWith( "field" -> "is a null" )
//    }
  }
}

object ValidationTransformTests {
  import dsl._
  
  case class FlatTest( field: String )
  val implicitlyDescribedNamedValidator = validator[ FlatTest ] { t => t.field is notNull }
  val implicitlyDescribedAnonymousValidator = validator[ FlatTest ] { _.field is notNull }
  val explicitlyDescribedValidator = validator[ FlatTest ] { t => t.field as "described" is notNull }
  val implicitlyDescribedValueValidator = validator[ String ] { _ is notNull }
//  val adaptedValidator = implicitlyDescribedValueValidator compose { ( f: FlatTest ) => f.field }

  case class CompositeTest( member: FlatTest )
  val compositeValidator = {
    implicit val flatValidator = implicitlyDescribedAnonymousValidator
    validator[ CompositeTest ] { _.member is valid }
  }
  val namedIndirectValidator = validator[ CompositeTest ] { c => c.member.field is notNull }
  val anonymousIndirectValidator = validator[ CompositeTest ] { _.member.field is notNull }
}