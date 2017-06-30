/*
  Copyright 2013-2017 Wix.com

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

import com.wix.accord.Validator
import com.wix.accord.combinators.StartsWith
import com.wix.accord.combinators.EndsWith
import com.wix.accord.combinators.MatchesRegex
import com.wix.accord.combinators.NotBlank
import com.wix.accord.combinators.Blank

import scala.util.matching.Regex
import java.util.regex.Pattern

/** Provides a DSL for string validators. */
trait StringOps {
  /** Specifies a validator that operates on strings and succeeds only if the validation expression starts with
    * the specified prefix.
    */
  def startWith( prefix: String ): Validator[ String ] = new StartsWith( prefix )

  /** Specifies a validator that operates on strings and succeeds only if the validation expression ends with
    * the specified suffix.
    */
  def endWith( suffix: String ): Validator[ String ] = new EndsWith( suffix )

  /** Specifies a validator that operates on strings and succeeds only if the validation expression is not blank
    * (i.e. empty or whitespace-only).
    */
  def notBlank: Validator[ String ] = new NotBlank

  /** Specifies a validator that operates on strings and succeeds only if the validation expression is blank
    * (i.e. empty or whitespace-only).
    */
  def blank: Validator[ String ] = new Blank

  /** Specifies a validator that operates on strings and succeeds only if the validation expression matches the
    * specified regular expression.
    */
  def matchRegex( regex: String ): Validator[ String ] = matchRegex( regex.r )


  /** Specifies a validator that operates on strings and succeeds only if the validation expression matches the
    * specified regular expression.
    */
  def matchRegex( regex: Regex ): Validator[ String ] = matchRegex( regex.pattern )

  /** Specifies a validator that operates on strings and succeeds only if the validation expression matches the
    * specified regular expression.
    */
  def matchRegex( pattern: Pattern ): Validator[ String ] = new MatchesRegex( pattern, partialMatchAllowed = true )

  /** Specifies a validator that operates on strings and succeeds only if the validation expression **fully**
    * matches the specified regular expression. See [[com.wix.accord.combinators.StringCombinators.MatchesRegex]]
    * for a full explanation of the difference between partial and full matching.
    */
  def matchRegexFully( regex: String ): Validator[ String ] = matchRegexFully( regex.r )

  /** Specifies a validator that operates on strings and succeeds only if the validation expression **fully**
    * matches the specified regular expression. See [[com.wix.accord.combinators.StringCombinators.MatchesRegex]]
    * for a full explanation of the difference between partial and full matching.
    */
  def matchRegexFully( regex: Regex ): Validator[ String ] = matchRegexFully( regex.pattern )

  /** Specifies a validator that operates on strings and succeeds only if the validation expression **fully**
    * matches the specified regular expression. See [[com.wix.accord.combinators.StringCombinators.MatchesRegex]]
    * for a full explanation of the difference between partial and full matching.
    */
  def matchRegexFully( pattern: Pattern ): Validator[ String ] = new MatchesRegex( pattern, partialMatchAllowed = false )
}
