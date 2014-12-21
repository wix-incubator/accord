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

package com.wix.accord.combinators

import java.util.regex.Pattern

import com.wix.accord._

trait StringCombinatorConstraints {
  self: Constraints =>
  
  protected def startsWithConstraint( prefix: String ): Constraint
  protected def endsWithConstraint( suffix: String ): Constraint
  protected def matchRegexConstraint( pattern: Pattern ): Constraint
  protected def fullyMatchRegexConstraint( pattern: Pattern ): Constraint
}

/** Combinators that operate specifically on strings. */
trait StringCombinators extends BaseValidators with ResultBuilders {
  self: Validation with Results with Constraints with StringCombinatorConstraints =>

  /** A validator that succeeds only if the provided string starts with the specified prefix. */
  class StartsWith( prefix: String )
    extends NullSafeValidator[ String ]( _ startsWith prefix, _ -> startsWithConstraint( prefix ) )

  /** A validator that succeeds only if the provided string starts with the specified suffix. */
  class EndsWith( suffix: String )
    extends NullSafeValidator[ String ]( _ endsWith suffix, _ -> endsWithConstraint( suffix ) )

  /** A validator that succeeds only if the provided string matches the specified pattern.
    *
    * @param partialMatchAllowed Whether or not the pattern has to match the entire string or any part of it. For
    *                            example, the pattern `\s+def` only partially matches `abc def`; the leading `abc`
    *                            will fail the match if partialMatchAllowed is false. This reflects the difference
    *                            between [[java.util.regex.Matcher.find]] and [[java.util.regex.Matcher.matches]].
    */
  class MatchesRegex( pattern: Pattern, partialMatchAllowed: Boolean = true )
    extends NullSafeValidator[ String ](
      v => if ( partialMatchAllowed ) pattern.matcher( v ).find() else pattern.matcher( v ).matches(),
      v => if ( partialMatchAllowed ) v -> matchRegexConstraint( pattern )
                                 else v -> fullyMatchRegexConstraint( pattern ) )
}
