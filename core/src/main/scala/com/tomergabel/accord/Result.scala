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

trait Violation extends Iterable[ RuleViolation ] {
  def value: Any
  def constraint: String

  private[ accord ] def withDescription( rewrite: String ): Violation
}
case class RuleViolation( value: Any, constraint: String, description: String ) extends Violation {
  private[ accord ] def withDescription( rewrite: String ) = this.copy( description = rewrite )
  def iterator = Iterator( this )
}
case class GroupViolation( value: Any, constraint: String, rules: Seq[ Violation ] ) extends Violation {
  private[ accord ] def withDescription( rewrite: String ) =
    this.copy( rules = this.rules map { _ withDescription rewrite } )

  def iterator = rules.flatten.iterator
}

/** A base trait for validation results.
  * @see [[com.tomergabel.accord.Success]], [[com.tomergabel.accord.Failure]]
  */
sealed trait Result {
  def and( other: Result ): Result
  def or( other: Result ): Result
}

/** An object representing a successful validation result. */
case object Success extends Result {
  def and( other: Result ) = other
  def or( other: Result ) = this
}

/** An object representing a failed validation result.
  * @param violations The violations that caused the validation to fail.
  */
case class Failure( violations: Seq[ Violation ] ) extends Result {
  def and( other: Result ) = other match {
    case Success => this
    case Failure( vother ) => Failure( violations ++ vother )
  }
  def or( other: Result ) = other match {
    case Success => other
    case Failure(_) => this
  }
}
