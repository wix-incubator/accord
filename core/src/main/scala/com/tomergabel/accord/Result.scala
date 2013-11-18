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

// TODO work on the description/messaging infrastructure

/** Each violation represents failure in a single validation rule. Multiple violations may be aggregated into
  * a single [[com.tomergabel.accord.Failure]] instance.
  * @param constraint A textual description of the constraint which failed validation.
  * @param value The value that caused the failure.
  */
case class Violation( constraint: String, value: Any )

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
