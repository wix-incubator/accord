/*
  Copyright 2013-2014 Wix.com

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

package com.wix.accord.tests.transform

import com.wix.accord.{TestDomain, TestDomainMatchers}
import org.scalatest.{Matchers, WordSpec}
import TestDomain._

class ValidationTransformTests extends WordSpec with TestDomainMatchers with Matchers {
  import ValidationTransformTests._
  
  "Validator description" should {
    "be generated for a fully-qualified field selector" in {
      validate( FlatTest( null ) )( implicitlyDescribedNamedValidator ) should failWith( "field" -> Constraints.IsNotNull )
    }
    "be generated for an anonymously-qualified field selector" in {
      validate( FlatTest( null ) )( implicitlyDescribedAnonymousValidator ) should failWith( "field" -> Constraints.IsNotNull )
    }
    "be generated for an anonymous value reference" in {
      validate( null )( implicitlyDescribedValueValidator ) should failWith( "value" -> Constraints.IsNotNull )
    }
    "be generated for a fully-qualified selector with multiple indirections" in {
      val obj = CompositeTest( FlatTest( null ) )
      validate( obj )( namedIndirectValidator ) should failWith( "member.field" -> Constraints.IsNotNull )
    }
    "be generated for an anonymously-qualified selector with multiple indirections" in {
      val obj = CompositeTest( FlatTest( null ) )
      validate( obj )( anonymousIndirectValidator ) should failWith( "member.field" -> Constraints.IsNotNull )
    }
    "be generated for a multiple-clause boolean expression" in {
      val obj = FlatTest( "123" )
      validate( obj )( booleanExpressionValidator ) should failWith(
        group( null, Constraints.NoMatch,
          "field" -> Constraints.IsNull,
          "field" -> Constraints.GreaterThan( 5 )
        ) )
    }
    "be propagated for an explicitly-described expression" in {
      validate( FlatTest( null ) )( explicitlyDescribedValidator ) should failWith( "described" -> Constraints.IsNotNull )
    }
    "be propagated for a composite validator" in {
      val obj = CompositeTest( FlatTest( null ) )
      validate( obj )( compositeValidator ) should
        failWith( group( description        = "member",
                         constraint         = Constraints.Invalid,
                         expectedViolations = "field" -> Constraints.IsNotNull ) )
    }
    "be propagated for an adapted validator" in {
      validate( FlatTest( null ) )( adaptedValidator ) should failWith( "field" -> Constraints.IsNotNull )
    }
  }
}

object ValidationTransformTests {
  import TestDomain._
  import dsl._

  case class FlatTest( field: String )
  val implicitlyDescribedNamedValidator = validator[ FlatTest ] { t => t.field is notNull }
  val implicitlyDescribedAnonymousValidator = validator[ FlatTest ] { _.field is notNull }
  val explicitlyDescribedValidator = validator[ FlatTest ] { t => t.field as "described" is notNull }
  val implicitlyDescribedValueValidator = validator[ String ] { _ is notNull }
  val adaptedValidator = implicitlyDescribedValueValidator compose { ( f: FlatTest ) => f.field }
  val booleanExpressionValidator = validator[ FlatTest ] { t => ( t.field is aNull ) or ( t.field has size > 5 ) }

  case class CompositeTest( member: FlatTest )
  val compositeValidator = {
    implicit val flatValidator = implicitlyDescribedAnonymousValidator
    validator[ CompositeTest ] { _.member is valid }
  }
  val namedIndirectValidator = validator[ CompositeTest ] { c => c.member.field is notNull }
  val anonymousIndirectValidator = validator[ CompositeTest ] { _.member.field is notNull }
}