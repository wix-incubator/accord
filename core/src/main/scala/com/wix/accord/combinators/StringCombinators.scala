/*
  Copyright 2013-2016 Wix.com

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

import com.wix.accord.NullSafeValidator
import com.wix.accord.ViolationBuilder._
import java.util.regex.Pattern

/** Combinators that operate specifically on strings. */
trait StringCombinators {

  /** A validator that succeeds only if the provided string starts with the specified prefix. */
  class StartsWith( prefix: String )
    extends NullSafeValidator[ String ]( _ startsWith prefix, _ -> s"must start with '$prefix'" )

  /** A validator that succeeds only if the provided string starts with the specified suffix. */
  class EndsWith( suffix: String )
    extends NullSafeValidator[ String ]( _ endsWith suffix, _ -> s"must end with '$suffix'" )

  /** A validator that succeeds only if the provided string is not blank. Note that [[java.lang.String.trim]] is used
    * for inspection whitespace.
    */
  class NotBlank
    extends NullSafeValidator[ String ]( !_.trim.isEmpty, _ -> "must not be blank" )

  /** A validator that succeeds only if the provided string is blank. Note that [[java.lang.String.trim]] is used
    * for inspection whitespace.
    */
  class Blank
    extends NullSafeValidator[ String ]( _.trim.isEmpty, _ -> "must be blank" )

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
      v => if ( partialMatchAllowed ) v -> s"must match regular expression '$pattern'"
                                 else v -> s"must fully match regular expression '$pattern'" )
}
