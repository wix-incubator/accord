---
layout: page
title: "The Accord API"
---

# Overview

Accord's API comprises three main building blocks:

* [Execution](#execution) and discovery;
* A result [domain model](#result-model) and [description model](#description-model);
* A dedicated [DSL](dsl.html) and [combinator library](dsl.html#combinator-library) for defining validation rules.

# Execution
<a name="execution"></a>

As with any validation framework, Accord faces two design considerations: discovery (which validator should be executed? What happens if there are multiple options?) and execution (running the actual validator and obtaining the results). Accord solves these dilemmas by:
  
* Defining `Validator[T]` as a function from some type under validation `T` to Accord's [result model](#result-model);
* Making `Validator[T]` a typeclass, and leveraging Scala's normal implicit resolution mechanism for discovery.

This provides a very flexible API with which to scope and execute validators. The recommended practice is to place a class's validator in its companion object, thereby making it automatically visible to anyone using the class, but validators can be placed anywhere and resolved explicitly if needed:

```scala
case class Person( name: String, age: Int )
case object Person {
  import com.wix.accord.dsl._

  // The following validator is automatically included in implicit search scope,
  // so users of the Person class do not have to explicitly import it.
  implicit val personValidator = validator[ Person ] { p =>
    p.name is notEmpty
    p.age should be >= 18
  }
}


import com.wix.accord._       // Import the Accord root API

val person = Person( "Sherlock Holmes", 27 )

// A suitable validator is automatically resolved from Person's companion object:
val result1 = validate( person )

// You can also specify an explicit validator:
val result2 = validate( person )( Person.personValidator )

// Since a validator is a function, you can also explicitly apply it:
val result3 = Person.personValidator( person )

// All three are equivalent!
assert( result1 == result2 && result2 == result3 )
```

# Results
<a name="result-model"></a>

The Accord domain model is quite simple (following is a minimal excerpt, for the full API definition see [Result.scala](https://github.com/wix/accord/blob/v{{ site.version.release }}/api/src/main/scala/com/wix/accord/Result.scala)):

```scala
// A validation result can either be a success or a failure:
sealed trait Result
case object Success extends Result
case class Failure( violations: Set[ Violation ] ) extends Result

// Failures aggregate one or more violations:
trait Violation {
  /** The actual value that failed validation, e.g. 15 */
  def value: Any                     
  /** The violated constraint, e.g. "got 15, expected 18 or more" */
  def constraint: String
  /** The actual generated path of the object under validation, more details below */
  def path: Path
}
```

The vast majority of violations indicate a broken *rule*; some validators, notably delegation via `valid` and the `or` operator, produce a violation that encompasses a *group* of violations. Consider the following example:

```scala
case class Address( street: String, city: String, zipcode: Option[ String ] )
case class Person( name: String, age: Int, address: Address )

import com.wix.accord.dsl._

implicit val addressValidator = validator[ Address ] { a =>
  a.street is notEmpty
  a.city is notEmpty
  a.zipcode.each is notEmpty
}

implicit val personValidator = validator[ Person ] { p =>
  p.name is notEmpty
  p.age should be >= 18
  p.address is valid      // Implicitly delegates to addressValidator
}
```

What happens if you validate an instance of `Person` that has an empty name? A *rule* has been broken, and consequently you'd expect a single violation. But what if the person's address is also bad, and in fact has multiple violations? One can imagine two possibilities:

* Generate multiple violations, and mark each with a "path" to the relevant expression (e.g. `p.address.street`). This has the benefit of only requiring a single abstraction, but becomes quite unwieldy with multiple indirections, indexing into sequences and other "path" types.
* Add a container type to the domain model. While it makes the model more complex, it has the benefit of added flexibility on top of the full context.

Accord takes the second approach, and provides two built-in violation types: `RuleViolation` and `GroupViolation` (see [full API](https://github.com/wix/accord/blob/v{{ site.version.release }}/api/src/main/scala/com/wix/accord/Result.scala)). Based on the above example, here's a sample validation output:

```scala

import com.wix.accord._
val result = validate( Person( "", 27, Address( "221B Baker Street", "", Some( "" ) ) ) )

assert( result == Failure( Set(
  // First violation:
  RuleViolation( "", "must not be empty", Path( Generic( "name" ) ) ),

  // Second violation:
  GroupViolation(
    value       = Address( "221B Baker Street", "", Some( "" ) ),
    constraint  = "is invalid",
    description = Path( Generic( "address" ) ),
    children = Set(
      RuleViolation( "", "must not be empty", Path( Generic( "city" ) ) ),
      RuleViolation( "", "must not be empty", Path( Generic( "zipcode" ) ) 
    )
  )
) ) )
```

# Descriptions
<a name="description-model"></a>

Accord automatically generates descriptions for each validation rule based on the expression left of `is`. Since a rule can validate any arbitrary Scala expression, Accord features a fine-grained [description model](https://github.com/wix/accord/blob/v{{ site.version.release }}/api/src/main/scala/com/wix/accord/Descriptions.scala) which is exposed through a violation's `path` property.

Any violation can be traced through a `Path`, which is a sequence of "steps" into the domain model. An empty path means the root object itself failed a validation rule, but it is much more common in practice to see paths leading to specific properties. Each "step" in the path is encoded as an instance of the `Description` trait, with the following subclasses available:


|--------------+--------------------------------------------------------------------------------------------------+
| Class        | Description                                                                                      |
|--------------+--------------------------------------------------------------------------------------------------+
| Explicit     | An explicitly described validation rule (via the `as` DSL operator)                              |
| Generic      | A textual description when Accord can't break the expression down further (e.g. property name)   |
| Indexed      | Indicates indexed access, such as into a sequence                                                |
| Branch       | Denotes that the desirable validation strategy depends of the result of an `if` statement        |
| PatternMatch | Denotes that the desirable validation strategy depends on the result of a pattern match          |
|--------------|--------------------------------------------------------------------------------------------------+

With this model, Accord automatically produces detailed information about the exact object that violated a particular rule, for example:

```scala
import com.wix.accord._
import com.wix.accord.dsl._
import com.wix.accord.Descriptions._     // For assertions

// First set up a sample domain object and validator:

case class Person( age: Int,
                   guardian: Option[ String ], 
                   ssn: Option[ String ], 
                   heightInMeters: Double, 
                   weightInKG: Double )

def bmi( heightInMeters: Double, weightInKG: Double ) =
  weightInKG / ( heightInMeters * heightInMeters )

implicit val personValidator = validator[ Person ] { p =>
  p.age as "Legal age" must be >= 0
  p.heightInMeters must be >= 0.0
  p.weightInKG must be >= 0.0
  bmi( p.heightInMeters, p.weightInKG ) must be <= 25.0
  if ( p.age < 18 )
    p.guardian is notEmpty
  else
    p.ssn is notEmpty
}

val person = Person( 18, Some( "Super Dad" ), Some( "078-05-1120" ), 2, 100 )


// Now we can execute various failed violations and examine
// the resulting descriptions.
//
// 1. Explicitly described fields (using the 'as' keyword):

val explicit = validate( person.copy( age = -1 ) )
assert( explicit == Failure( Set(
  RuleViolation(
    value = -1,
    constraint = "got -1, expected 0 or more",
    path = Path( Explicit( "Legal age" ) )
  )
) ) )

// 2. Named property access:

val propertyAccess = validate( person.copy( heightInMeters = -4.0 ) )
assert( propertyAccess == Failure( Set(
  RuleViolation(
    value = -4.0,
    constraint = "got -4.0, expected 0.0 or more",
    path = Path( Generic( "heightInMeters" ) )
  )
) ) )

// 3. Arbitrary expressions:

val generic = validate( person.copy( weightInKG = 120 ) )
assert( generic == Failure( Set(
  RuleViolation(
    value = 30.0,
    constraint = "got 30.0, expected 25.0 or less",
    path = Path( Generic( "bmi( p.heightInMeters, p.weightInKG )" ) )
  )
) ) )

// 4. Branch (if/else):

val branch = validate( person.copy( age = 1, guardian = None ) )
assert( branch == Failure( Set(
  RuleViolation(
    value = None,
    constraint = "must not be empty",
    path = Path(
      Branch( guard = Generic( "p.age < 18" ), evaluation = true ),
      Generic( "guardian" )
    )
  )
) ) )
```

Dealing with these descriptions can be a bit of a chore, and Accord provides a simple string-rendering function (`Descriptions.render`):

```scala
def descriptionOf( r: Result ) = 
  Descriptions.render( r.toFailure.get.violations.head.path )

assert( descriptionOf( explicit ) == "Legal age" )
assert( descriptionOf( generic ) == "bmi( p.heightInMeters, p.weightInKG )" )
assert( descriptionOf( propertyAccess ) == "heightInMeters" )
assert( descriptionOf( branch ) == "value [where p.age < 18 is true].guardian" )
```
