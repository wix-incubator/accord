/*
  Copyright 2013-2015 Wix.com

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

import com.wix.accord.Descriptions._
import org.scalatest.{Matchers, WordSpec}
import com.wix.accord._
import com.wix.accord.scalatest.ResultMatchers

class ValidationTransformTests extends WordSpec with Matchers with ResultMatchers {
  import ValidationTransformTests._

  "An \"if\" control structure that resolves to a validation rule" should {
    import ControlStructures._

    "dispatch to the true branch if at runtime if the condition is met" in {
      val trueBranchValid = ControlStructureTest( -5, "-5" )
      val trueBranchInvalid = ControlStructureTest( -5, "5" )
      ifWithBothBranches( trueBranchValid ) shouldBe aSuccess
      ifWithBothBranches( trueBranchInvalid ) shouldBe aFailure
    }

    "dispatch to the false branch at runtime if the condition is not met" in {
      val falseBranchValid = ControlStructureTest( 5, "5" )
      val falseBranchInvalid = ControlStructureTest( 5, "" )
      ifWithBothBranches( falseBranchValid ) shouldBe aSuccess
      ifWithBothBranches( falseBranchInvalid ) shouldBe aFailure
    }

    "correctly handle empty branches" in {
      val trueBranchValid = ControlStructureTest( -5, "-5" )
      val trueBranchInvalid = ControlStructureTest( -5, "5" )
      val falseBranch = ControlStructureTest( 5, "whatever" )
      ifWithNoElse( trueBranchInvalid ) shouldBe aFailure
      ifWithNoElse( trueBranchValid ) shouldBe aSuccess
      ifWithNoElse( falseBranch ) shouldBe aSuccess
    }

    "correctly handle multiple rules in a branch" in {
      val falseBranch = ControlStructureTest( 5, "" )
      val trueBranchInvalid = ControlStructureTest( -5, "" )
      ifWithMultipleRules( falseBranch ) shouldBe aSuccess
      ifWithMultipleRules( trueBranchInvalid ) shouldBe aFailure
   }

    // TODO a proper Conditional matcher builder, then:
    // TODO split into separate tests for each property
    "describe the true branch correctly" in {
      val trueBranchInvalid = ControlStructureTest( -5, "5" )
      ifWithBothBranches( trueBranchInvalid ) should failWith( Conditional(
        on = Generic( "branch" ),
        value = true,
        guard = Some( Generic( "cst.field1 < 0" ) ),
        target = AccessChain( Generic( "field2" ) )
      ) )
    }

    "describe the false branch correctly" in {
      val falseBranchInvalid = ControlStructureTest( 5, "" )
      ifWithBothBranches( falseBranchInvalid ) should failWith( Conditional(
        on = Generic( "branch" ),
        value = false,
        guard = Some( Generic( "<else>" ) ),
        target = AccessChain( Generic( "field2" ) )
      ) )
    }

    "derive the guard description on non-terminal branches of an if-else chain" in {
      val secondBranchInvalid = ControlStructureTest( 0, "5" )
      ifElseChain( secondBranchInvalid ) should failWith( Conditional(
        on = Generic( "branch" ),
        value = true,
        guard = Some( Generic( "cst.field1 == 0" ) ),
        target = AccessChain( Generic( "field2" ) )
      ) )
    }

    "describe the last branch of an if-else chain correctly" in {
      val thirdBranchInvalid = ControlStructureTest( 5, "" )
      ifElseChain( thirdBranchInvalid ) should failWith( Conditional(
        on = Generic( "branch" ),
        value = false,
        guard = Some( Generic( "<else>" ) ),
        target = AccessChain( Generic( "field2" ) )
      ) )
    }
  }

  "A pattern match that resolves to a validation rule" should {
    import ControlStructures._

    "dispatch to the correct case at runtime" in {
      val valid1 = ControlStructureTest( 1, "1" )
      val invalid1 = ControlStructureTest( 1, "wrong" )
      val valid2 = ControlStructureTest( 2, "2" )
      val invalid2 = ControlStructureTest( 2, "wrong" )
      simplePatternMatch( valid1 ) shouldBe aSuccess
      simplePatternMatch( invalid1 ) shouldBe aFailure
      simplePatternMatch( valid2 ) shouldBe aSuccess
      simplePatternMatch( invalid2 ) shouldBe aFailure
    }
    "support multiple validation rules" in {
      val valid1 = ControlStructureTest( 1, "12345" )
      val valid2 = ControlStructureTest( 2, "20" )
      val invalid1 = ControlStructureTest( 1, "wrong" )
      val invalid2 = ControlStructureTest( 1, "" )
      patternMatchWithMultipleRules( valid1 ) shouldBe aSuccess
      patternMatchWithMultipleRules( invalid1 ) shouldBe aFailure
      patternMatchWithMultipleRules( valid2 ) shouldBe aSuccess
      patternMatchWithMultipleRules( invalid2 ) shouldBe aFailure
    }
    "correctly describe a case on failure" in {
      val invalid = ControlStructureTest( 1, "wrong" )
      simplePatternMatch( invalid ) should failWith( Conditional(
        on = AccessChain( Generic( "field1" ) ),
        value = 1,
        guard = None,
        target = AccessChain( Generic( "field2" ) )  // TODO elide from test once we have a proper matcher in place
      ) )
    }
    "correctly describe a guard on failure" in {
      val invalid = ControlStructureTest( -1, "wrong" )
      patternMatchWithGuard( invalid ) should failWith( Conditional(
        on = AccessChain( Generic( "field1" ) ),
        value = -1,
        guard = Some( Generic( "n < 0" ) ),
        target = AccessChain( Generic( "field2" ) )
      ) )
    }
    "treat an empty case as an implicit success" in {
      val valid1 = ControlStructureTest( 1, "1" )
      val invalid1 = ControlStructureTest( 1, "wrong" )
      val validDefault = ControlStructureTest( -5, "all good" )
      patternMatchWithEmptyDefault( valid1 ) shouldBe aSuccess
      patternMatchWithEmptyDefault( invalid1 ) shouldBe aFailure
      patternMatchWithEmptyDefault( validDefault ) shouldBe aSuccess
    }
  }

  "A static validator description" should {
    import Descriptions.Static._

    // TODO fold these tests into ExpressionDescriberTest if necessary, only test propagation herein

    "be generated for a fully-qualified field selector" in {
      validate( FlatTest( null ) )( implicitlyDescribedNamedValidator ) should
        failWith( AccessChain( Generic( "field" ) ) -> "is a null" )
    }
    "be generated for an anonymously-qualified field selector" in {
      validate( FlatTest( null ) )( implicitlyDescribedAnonymousValidator ) should
        failWith( AccessChain( Generic( "field" ) ) -> "is a null" )
    }
    "be generated for an anonymous value reference" in {
      validate( null )( implicitlyDescribedValueValidator ) should failWith( SelfReference -> "is a null" )
    }
    "be generated for a fully-qualified selector with multiple indirections" in {
      val obj = CompositeTest( FlatTest( null ) )
      validate( obj )( namedIndirectValidator ) should
        failWith( AccessChain( Generic( "member" ), Generic( "field" ) ) -> "is a null" )
    }
    "be generated for an anonymously-qualified selector with multiple indirections" in {
      val obj = CompositeTest( FlatTest( null ) )
      validate( obj )( anonymousIndirectValidator ) should
        failWith( AccessChain( Generic( "member" ), Generic( "field" ) ) -> "is a null" )
    }
    "be generated for a multiple-clause boolean expression" in {
      val obj = FlatTest( "123" )
      validate( obj )( booleanExpressionValidator ) should failWith(
        group( null: Description, "doesn't meet any of the requirements",
          AccessChain( Generic( "field" ) ) -> "is not a null",
          AccessChain( Generic( "field" ) ) -> "has size 3, expected more than 5"
        ) )
    }
    "be generated for a generic expression" in {
      val obj = FlatTest( "" )
      validate( obj )( genericValidator ) should
        failWith( Generic( "t.field.length * 2" ) -> "got 0, expected more than 0" )
    }
    "be propagated for an explicitly-described expression" in {
      validate( FlatTest( null ) )( explicitlyDescribedValidator ) should
        failWith( Explicit( "described" ) -> "is a null" )
    }
    "be propagated for a composite validator" in {
      val obj = CompositeTest( FlatTest( null ) )
      validate( obj )( compositeValidator ) should failWith( group(
        AccessChain( Generic( "member" ) ),
        "is invalid",
        AccessChain( Generic( "field" ) ) -> "is a null"
      ) )
    }
    "be propagated for an adapted validator" in {
      validate( FlatTest( null ) )( adaptedValidator ) should
        failWith( AccessChain( Generic( "field" ) ) -> "is a null" )
    }
  }

  "A runtime validator description decoration (e.g. sequence position)" should {
    import Descriptions.Runtime._

    "propagate correctly when the object under validation is implicitly described" in {
      val sample = RuntimeDescribedTest( Seq( "valid", "" ) )
      val result = validate( sample )( implicitDescriptionWithRuntimeRewriteValidator )
      result should failWith( Indexed( 1L, AccessChain( Generic( "field" ) ) ) -> "must not be empty" )
    }

    "propagate correctly when the object under validation is explicitly described with \"as\"" in {
      val sample = RuntimeDescribedTest( Seq( "valid", "" ) )
      val result = validate( sample )( explicitDescriptionWithRuntimeRewriteValidator )
      result should failWith( Indexed( 1L, Explicit( "described" ) ) -> "must not be empty" )
    }

    "support interleaved indices within access chains" in {
      val sample = CollectionOfIndirections( Seq( TestElement( "" ) ) )
      val result = validate( sample )( collectionOfIndirectionsValidator )
      result should failWith( AccessChain( Indexed( 0L, AccessChain( Generic( "field" ) ) ), Generic( "property" ) ) )
    }
  }

  "Validation block transformation" should {

    "safely ignore statements of type Nothing" in {
      """
      import com.wix.accord.dsl._

      val v = validator[ String ] { s => throw new Exception() }
      """ should compile
    }

    "safely ignore statements of type Null" in {
      """
      import com.wix.accord.dsl._

      def _null: Null = null
      val v = validator[ String ] { s => _null }
      """ should compile
    }
  }
}

object ValidationTransformTests {
  import dsl._

  object Descriptions {

    object Static {
      case class FlatTest( field: String )
      val implicitlyDescribedNamedValidator = validator[ FlatTest ] { t => t.field is notNull }
      val implicitlyDescribedAnonymousValidator = validator[ FlatTest ] { _.field is notNull }
      val explicitlyDescribedValidator = validator[ FlatTest ] { t => t.field as "described" is notNull }
      val implicitlyDescribedValueValidator = validator[ String ] { _ is notNull }
      val adaptedValidator = implicitlyDescribedValueValidator compose { ( f: FlatTest ) => f.field }
      val booleanExpressionValidator = validator[ FlatTest ] { t => ( t.field is aNull ) or ( t.field has size > 5 ) }
      val genericValidator = validator[ FlatTest ] { t => t.field.length * 2 must be > 0 }

      case class CompositeTest( member: FlatTest )
      val compositeValidator = {
        implicit val flatValidator = implicitlyDescribedAnonymousValidator
        validator[ CompositeTest ] { _.member is valid }
      }
      val namedIndirectValidator = validator[ CompositeTest ] { c => c.member.field is notNull }
      val anonymousIndirectValidator = validator[ CompositeTest ] { _.member.field is notNull }
    }

    object Runtime {
      case class RuntimeDescribedTest( field: Seq[ String ] )
      val implicitDescriptionWithRuntimeRewriteValidator =
        validator[ RuntimeDescribedTest ] { rdt => rdt.field.each is notEmpty }
      val explicitDescriptionWithRuntimeRewriteValidator =
        validator[ RuntimeDescribedTest ] { rdt => ( rdt.field as "described" ).each is notEmpty }


      case class TestElement( property: String )
      case class CollectionOfIndirections( field: Seq[ TestElement ] )

      val aValidElement = validator[ TestElement ] { c => c.property is notEmpty }
      val collectionOfIndirectionsValidator =
        validator[ CollectionOfIndirections ] { coi => coi.field.each is aValidElement }
    }
  }

  object ControlStructures {
    case class ControlStructureTest( field1: Int, field2: String )
    val ifWithNoElse = validator [ ControlStructureTest ] { cst =>
      if ( cst.field1 < 0 )
        cst.field2 should startWith( "-" )
    }
    val ifWithMultipleRules = validator [ ControlStructureTest ] { cst =>
      if ( cst.field1 < 0 ) {
        cst.field2 is notEmpty
        cst.field2 should startWith( "-" )
      }
    }
    val ifWithBothBranches = validator[ ControlStructureTest ] { cst =>
      if ( cst.field1 < 0 )
        cst.field2 should startWith( "-" )
      else
        cst.field2 is notEmpty
    }
    val ifElseChain = validator[ ControlStructureTest ] { cst =>
      if ( cst.field1 < 0 )
        cst.field2 should startWith( "-" )
      else if ( cst.field1 == 0 )
        cst.field2 is equalTo( "0" )
      else
        cst.field2 is notEmpty
    }
    val simplePatternMatch = validator[ ControlStructureTest ] { cst =>
      cst.field1 match {
        case 1 => cst.field2 is equalTo( "1" )
        case 2 => cst.field2 is equalTo( "2" )
      }
    }
    val patternMatchWithMultipleRules = validator[ ControlStructureTest ] { cst =>
      cst.field1 match {
        case 1 =>
          cst.field2 is notEmpty
          cst.field2 should startWith( "1" )
      }
    }
    val patternMatchWithEmptyDefault = validator[ ControlStructureTest ] { cst =>
      cst.field1 match {
        case 1 => cst.field2 is equalTo( "1" )
        case _ =>
      }
    }
    val patternMatchWithGuard = validator[ ControlStructureTest ] { cst =>
      cst.field1 match {
        case n if n < 0 => cst.field2 should startWith( "-" )
        case 0          => cst.field2 is equalTo( "0" )
        case n if n > 0 => cst.field2 is notEmpty
      }
    }
  }
}