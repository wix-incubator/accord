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

package com.wix.accord.combinators

import com.wix.accord.{RuleViolation, Validator}
import java.util.regex.Pattern

/** Combinators that operate specifically on strings. */
trait StringCombinators {

  /** A validator that succeeds only if the provided string starts with the specified prefix. */
  class StartsWith( prefix: String ) extends Validator[ String ] {
    def apply( x: String ) = result( x startsWith prefix, RuleViolation( x, s"must start with '$prefix'", description ) )
  }

  /** A validator that succeeds only if the provided string starts with the specified suffix. */
  class EndsWith( suffix: String ) extends Validator[ String ] {
    def apply( x: String ) = result( x endsWith suffix, RuleViolation( x, s"must end with '$suffix'", description ) )
  }

  /** A validator that succeeds only if the provided string matches the specified pattern.
    *
    * @param partialMatchAllowed Whether or not the pattern has to match the entire string or any part of it. For
    *                            example, the pattern `\s+def` only partially matches `abc def`; the leading `abc`
    *                            will fail the match if partialMatchAllowed is false. This reflects the difference
    *                            between [[java.util.regex.Matcher.find]] and [[java.util.regex.Matcher.matches]].
    */
  class MatchesRegex( pattern: Pattern, partialMatchAllowed: Boolean = true ) extends Validator[ String ] {
    def apply( x: String ) =
      if ( partialMatchAllowed )
        result( pattern.matcher( x ).find(), RuleViolation( x, s"must match regular expression '$pattern'", description ) )
      else
        result( pattern.matcher( x ).matches(), RuleViolation( x, s"must fully match regular expression '$pattern'", description ) )
  }
}
