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

import scala.language.implicitConversions
import scala.language.experimental.macros
import com.tomergabel.accord.transform.ValidationTransform

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
  * These validators can later be executed via [[com.tomergabel.accord.validate]]. A macro rewrites each
  * validation block into a chain of validation rules at compile-time, and annotates accesses to getters
  * so that violation messages are automatically generated; for instance, the rule `p.firstName is notEmpty`
  * will generate the violation message "firstName must not be empty" automatically.
  */
package object dsl {
  import combinators._

  /** Takes a code block and rewrites it into a validation chain (see description in [[com.tomergabel.accord.dsl]].
    *
    * @param v The validation code block; may contain any combination of validation statements.
    * @tparam T The type under validation.
    * @return The validation code block rewritten as a [[com.tomergabel.accord.Validator]] for the specified type `T`.
    */
  def validator[ T ]( v: T => Unit ): Validator[ T ] = macro ValidationTransform.apply[ T ]

  private object Aggregates {
    private def aggregate[ U, E ]( validator: Validator[ E ], aggregator: Traversable[ Result ] => Result )
                                 ( implicit ev: U <:< Traversable[ E ] ) =
      new Validator[ U ] { def apply( col: U ) = aggregator( col map validator ) }

    def all[ U, E ]( validator: Validator[ E ] )( implicit ev: U <:< Traversable[ E ] ): Validator[ U ] =
      aggregate( validator, r => ( r fold Success )( _ and _ ) )
  }

  /** Wraps expressions under validation with a specialized scope (this is later used during the macro transform).
    * Enables syntax such as `p.firstName is notEmpty`, where `p.firstName` is the actual expression under
    * validation.
    *
    * @param value The value to wrap with a validation context.
    * @tparam U The type of the provided expression.
    */
  implicit class Contextualizer[ U ]( value: U ) {
    def is( validator: Validator[ _ >: U ] ) = validator
    def has( validator: Validator[ _ >: U ] ) = validator
    def have( validator: Validator[ _ >: U ] ) = validator
    def should( validator: Validator[ _ >: U ] ) = validator
    def must( validator: Validator[ _ >: U ] ) = validator

    /** Provides extended syntax for collections; enables validation rules such as `c.students.each is valid`.
      *
      * @param ev Evidence that the provided expression can be treated as a collection.
      * @tparam E The element type of the specified collection.
      * @return Additional syntax (see implementation).
      */
    def each[ E ]( implicit ev: U <:< Traversable[ E ] ) = new {
      def is( validator: Validator[ _ >: E ] ): Validator[ U ] = Aggregates.all( validator )
    }
  }

  /** Wraps expression under validation with an explicit description; after macro transformation, the resulting
    * validator will use the specified description to render violations. See the
    * [[com.tomergabel.accord.dsl.Descriptor.as]] method for an example.
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
      * personValidator: com.tomergabel.accord.combinators.And[Person] = <function1>
      *
      * scala> validate( Person( "", "last" ) )
      * res0: com.tomergabel.accord.Result = Failure(List(Violation(first name must not be empty,)))
      * ```
      *
      * With the explicit description, the violation would read "firstName must not be empty".
      *
      * @param description The description to use for the expression in case of violations.
      * @return The validation expression tagged with the explicit description.
      */
    def as( description: String ) = value
  }

  /** Extends validators with useful helpers.
    *
    * @param validator The validator to be extended.
    * @tparam T The type of the object under validation.
    */
  implicit class ValidatorBooleanOps[ T ]( validator: Validator[ T ] ) {
    def and( other: Validator[ T ] ) = new And( validator, other ) // TODO shortcut multiple ANDs
    def or( other: Validator[ T ] ) = new Or( validator, other )   // TODO shortcut multiple ORs
  }

  /** Specifies a validator that succeeds on empty instances; the object under validation must implement
    * `def isEmpty: Boolean` (see [[com.tomergabel.accord.combinators.HasEmpty]]).
    */
  def empty[ T <% HasEmpty ]: Validator[ T ] = new Empty[ T ]

  /** Specifies a validator that fails on empty instances; the object under validation must implement
    * `def isEmpty: Boolean` (see [[com.tomergabel.accord.combinators.HasEmpty]]).
    */
  def notEmpty[ T <% HasEmpty ]: Validator[ T ] = new NotEmpty[ T ]

  /** Provides access to size-based validators (where the object under validation must implement
    * `def size: Int`, see [[com.tomergabel.accord.combinators.HasSize]]). Enables syntax such as
    * `c.students has size > 0`.
    */
  def size[ T ] = new Size[ T ]

  /** Delegates validation to a pre-defined validation rule, which is encoded as an implicit
    * [[com.tomergabel.accord.Validator]] in scope. Enables composition of validation rules, as in:
    *
    * ```
    * case class Address( address1: String, address2: String, city: String, ... )
    * case class Item( sku: String, count: Int, ... )
    * case class Shipment( items: Seq[ Item ], address: Location, ... )
    *
    * implicit val addressValidator = validator[ Address ] { ... }
    * implicit val itemValidator = validator[ Item ] { ... }
    *
    * implicit val shipmentValidator = validator[ Shipment ] { shipment =>
    *   shipment.address is valid       // Implicitly uses addressValidator
    *   shipment.items.each is valid    // Implicitly uses itemValidator
    * }
    *
    * ```
    */
  def valid[ T ]( implicit validator: Validator[ T ] ): Validator[ T ] = new Valid[ T ]

  /** Specifies a validator that operates on strings and succeeds only if the validation expression starts with
    * the specified prefix.
    */
  def startsWith( prefix: String ): Validator[ String ] = new StartsWith( prefix )

  /** Specifies a validator that operates on strings and succeeds only if the validation expression ends with
    * the specified suffix.
    */
  def endsWith( suffix: String ): Validator[ String ] = new EndsWith( suffix )

  /** Specifies a validator that operates on strings and succeeds only if the validation expression matches the
    * specified regular expression.
    */
  def matches( regex: String ): Validator[ String ] = matches( regex.r.pattern )

  /** Specifies a validator that operates on strings and succeeds only if the validation expression matches the
    * specified regular expression.
    */
  def matches( pattern: java.util.regex.Pattern ): Validator[ String ] = new MatchesRegex( pattern, partialMatchAllowed = true )

  /** Specifies a validator that operates on strings and succeeds only if the validation expression **fully**
    * matches the specified regular expression. See [[com.tomergabel.accord.combinators.StringCombinators.MatchesRegex]]
    * for a full explanation of the difference between partial and full matching.
    */
  def fullyMatches( pattern: java.util.regex.Pattern ): Validator[ String ] = new MatchesRegex( pattern, partialMatchAllowed = false )

  /** Specifies a validator that operates on strings and succeeds only if the validation expression **fully**
    * matches the specified regular expression. See [[com.tomergabel.accord.combinators.StringCombinators.MatchesRegex]]
    * for a full explanation of the difference between partial and full matching.
    */
  def fullyMatches( regex: String ): Validator[ String ] = fullyMatches( regex.r.pattern )

  /** Specifies a validator that succeeds only if the validation expression is null. */
  def aNull: Validator[ AnyRef ] = new IsNull

  /** Specifies a validator that succeeds only if the validation expression is not null. */
  def notNull: Validator[ AnyRef ] = new IsNotNull

  /** Specifies a validator that succeeds only if the validation expression is equal to the specified value. Respects
    * nulls an performs equality checks via [[java.lang.Object.equals]].
    */
  def equalTo[ T ]( to: T ): Validator[ T ] = new EqualTo[ T ]( to )

  /** Specifies a validator that succeeds only if the validation expression is not equal to the specified value.
    * Respects nulls an performs equality checks via [[java.lang.Object.equals]].
    */
  def notEqualTo[ T ]( to: T ): Validator[ T ] = new NotEqualTo[ T ]( to )

  /** A proxy for ordering ops. Enables syntax such as `p.age should be > 5`. */
  val be = new OrderingOps
}
