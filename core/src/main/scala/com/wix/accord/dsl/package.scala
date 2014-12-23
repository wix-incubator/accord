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

package com.wix.accord.dsl

import com.wix.accord.Domain

import scala.language.implicitConversions
import scala.language.experimental.macros
import com.wix.accord.transform.ValidationTransform

/** Provides a DSL for defining validation rules. For example:
  *
  * ```
  * import dsl._    // Import the validator DSL
  *
  * case class Person( firstName: String, lastName: String )
  * case class Classroom( teacher: Person, students: Seq[ Person ] )
  *
  * implicit val personValidator = validator[ Person ] { p =>
  *   p.firstName is notEmpty
  *   p.lastName is notEmpty
  * }
  *
  * implicit val classValidator = validator[ Classroom ] { c =>
  *   c.teacher is valid        // Implicitly relies on personValidator!
  *   c.students.each is valid
  *   c.students have size > 0
  * }
  * ```
  *
  * These validators can later be executed via [[com.wix.accord.Validation#validate]]. A macro transforms each
  * validation block into a chain of validation rules at compile-time, and annotates accesses to getters
  * so that violation messages are automatically generated; for instance, the rule `p.firstName is notEmpty`
  * will generate the violation message "firstName must not be empty" automatically.
  */
trait DSL
  extends StringOps
     with CollectionOps
     with GenericOps
//     with OrderingOps
  with OrderingContext
     with BooleanOps
     with Contexts {
  
  self: Domain =>

  /** Takes a code block and rewrites it into a validation chain (see description in [[com.wix.accord.dsl]].
    *
    * @param v The validation code block; may contain any combination of validation statements.
    * @tparam T The type under validation.
    * @return The validation code block rewritten as a [[com.wix.accord.Validation#Validator]] for the
    *         specified type `T`.
    */
  def validator[ T ]( v: T => Unit ): Validator[ T ] = macro ValidationTransform.apply[ T ]

  /** Wraps expressions under validation with a specialized scope (this is later used during the macro transform).
    * Enables syntax such as `p.firstName is notEmpty`, where `p.firstName` is the actual expression under
    * validation.
    *
    * @param value The value to wrap with a validation context.
    * @tparam U The type of the provided expression.
    */
  implicit class Contextualizer[ U ]( value: U ) extends SimpleDslContext[ U ]

  /** Wraps expression under validation with an explicit description; after macro transformation, the resulting
    * validator will use the specified description to render violations. See the
    * [[com.wix.accord.dsl.Descriptor.as]] method for an example.
    *
    * @param value The value to wrap with an explicit description.
    * @tparam U The type of the provided expression.
    */
  implicit class Descriptor[ U ]( value: U ) {
    /** Tags the specified validation expression with an explicit description. Enables syntax such as:
      * `p.firstName as "first name" is notEmpty`; violations for this validation rule will be rendered with the
      * specified expression (instead of the implicit rule), for example:
      *
      * ```
      * scala> case class Person( firstName: String, lastName: String )
      * defined class Person
      *
      * scala> implicit val personValidator = validator[ Person ] { p => p.firstName as "first name" is notEmpty }
      * personValidator: com.wix.accord.combinators.And[Person] = <function1>
      *
      * scala> validate( Person( "", "last" ) )
      * res0: com.wix.accord.Results#Result = Failure(List(Violation(first name must not be empty,)))
      * ```
      *
      * With the explicit description, the violation would read "firstName must not be empty".
      *
      * @param description The description to use for the expression in case of violations.
      * @return The validation expression tagged with the explicit description.
      */
    def as( description: String ) = value
  }

  /** A proxy for ordering ops. Enables syntax such as `p.age should be > 5`. */
  object be extends OrderingOps { protected override def snippet = "got" }
}
