---
layout: page
title: "Defining Validators"
---

> :warning: Note: The DSL and combinator library are a work in progress, and are likely to expand and change before a 1.0 release. Breaking changes are usually highlighted in the [release notes](https://github.com/wix/accord/tree/master/notes) adn covered in the [migration guide](migration-guide.html).


# Overview

Accord provides a convenient DSL for defining validation rules. To define a validator over some type `T`, import the `com.wix.accord.dsl` package, and invoke the `validator[ T ]` function (where `T` is your specific type under validation). You can then use the provided sample object to define various rules:

```scala

case class Person( name: String, age: Int )

import com.wix.accord.dsl._    // Import the validator DSL

implicit val personValidator = validator[ Person ] { p =>
  // Validation rules:
  p.name is notEmpty
  p.age should be >= 18
}
```

Accord adds an implicit logical `and` relation between the rules, so all rules must apply in order for the validation to be successful. You can specify as many rules as you like.

## Descriptions

Each validation rule has an associated description (accessible via the [Violation](https://github.com/wix/accord/blob/v{{ site.version.release }}/api/src/main/scala/com/wix/accord/Result.scala) trait). This description is automatically generated by Accord:

```scala
val result = validate( Person( "", 15 ) )
assert( result == Failure( Set(
  // Note that the description (the last parameter) is automatically
  // generated from the validation rules:
  RuleViolation( "", "must not be empty", Descriptions.AccessChain( "name" ) ),
  RuleViolation( 15, "got 15, expected 18 or more", Descriptions.AccessChain( "age" ) )
) ) )
```

You can also explicitly provide a description with the `as` modifier:

```scala
implicit val personValidator = validator[ Person ] { p =>
  p.name as "Full name" is notEmpty
  p.age as "Age" should be >= 18
}

val result = validate( Person( "", 15 ) )
assert( result == Failure( Set(
  // Note that the descriptions now match the provided strings:
  RuleViolation( "", "must not be empty", Descriptions.Explicit( "Full name" ) ),
  RuleViolation( 15, "got 15, expected 18 or more", Descriptions.Explicit( "Age" ) )
) ) )
```

## Control Structures

Accord supports branching validation rules out-of-the-box. Often times you may want the evaluation of specific rules to depend on the runtime value of another property; to that end, Accord supports several native Scala control structures, notably `if`s and pattern matching:

```scala
case class NumericPair( numeric: Int, string: String )

implicit val numericValidator = validator[ NumericPair ] { n =>
  if ( n.numeric < 0 )
    n.string should startWith( "-" )
  else if ( n.numeric == 0 )
    n.string is equalTo( "0" )
  else
    n.string is notEmpty
}

assert( validate( NumericPair( -5, "-5" ) ) == Success )

// Generated descriptions include detailed information about the resolved branch:
assert( validate( NumericPair( 0, "zero" ) ) == Failure( Set (
  RuleViolation( "zero", "does not equal 0",
    Descriptions.Conditional(
      on     = Descriptions.Generic( "branch" ), 
      value  = true,
      guard  = Some( Descriptions.Generic( "n.numeric == 0" ) ),
      target = Descriptions.AccessChain( "string" )
    )
  )
) ) )
```

You can also use pattern matching, including guards and default cases. With pattern matching, the `on` property of the resulting violation includes information on the value being matched:

```scala
// An equivalent validator using pattern matching:
implicit val numericValidator = validator[ NumericPair ] { pair =>
  pair.numeric match {
    case n if n < 0 => pair.string should startWith( "-" )
    case 0          => pair.string is equalTo( "0" )
    case n if n > 0 => pair.string is notEmpty
  }
}

assert( validate( NumericPair( -5, "-5" ) ) == Success )

// Note that the "on" property now includes the expression being matched over, and
// (as there's no condition), the "guard" property is missing:
assert( validate( NumericPair( 0, "zero" ) ) == Failure( Set (
  RuleViolation( "zero", "does not equal 0",
    Descriptions.Conditional(
      on     = Descriptions.AccessChain( "numeric" ), 
      value  = 0,
      guard  = None,
      target = Descriptions.AccessChain( "string" )
    )
  )
) ) )
```

# Combinators
<a name="combinator-library"></a>

Accord offers a built-in library of building blocks (called "combinators") that can be composed into more complex validation rules.

* General-purpose

```scala
// Equality
sample.field is equalTo( "value" )
sample.field is notEqualTo( "value" )

// Nullability (only applies to reference types)
sample.field is aNull
sample.field is notNull

// Types
sample.field is anInstanceOf[ String ]
sample.field is notAnInstanceOf[ List[_] ]

// Delegation
sample.field is valid    					// Implicitly, or
sample.field is valid( myOwnValidator )		// Explicitly
```

* Primitives

```scala

// Booleans
sample.booleanField is true
sample.booleanField is false

// Strings
sample.stringField should startWith( "prefix" )
sample.stringField should endWith( "suffix" )
sample.stringField should matchRegex( "b[aeiou]t" )       // Matches "bat" and "dingbat"
sample.stringField should matchRegexFully( "b[aeiou]t" )  // Matches "bat" but not "dingbat"
sample.stringField should matchRegex( pattern )           // You can also use java.util.regex.Pattern
sample.stringField should matchRegex( regex )             // ... or scala.util.matching.Regex

// You can use "must" instead of "should":
sample.stringField must startWith( "prefix" )

// Strings are also "collection-like", so all collection combinators apply (see below)
sample.stringField is notEmpty

// Numerics (applies to any type with an instance of scala.math.Ordering in implicit search scope):
sample.intField should be < 5
sample.intField should be > 5
sample.intField should be <= 5
sample.intField should be >= 5
sample.intField should be == 5
sample.intField is between( 0, 10 )
sample.intField is between( 0, 10 ).exclusive
sample.intField is within( 0 to 10 )              // Inclusive
sample.intField is within( 0 until 10 )           // Exclusive
```

* Collections

```scala

// Existence in a collection
sample.entityType is in( "person", "address", "classroom" )

// Emptiness
sample.seq is empty
sample.seq is notEmpty
// This applies to any type that has a boolean "isEmpty" property, such as string)

// The "each" modifier applies the validation to all members of a collection:
sample.seq.each should be >= 10
sample.option.each should be >= 10                // Allows None or Some(15)

// Size (applies to any type with an integer "size" property)
// See "Numerics" above for additional operations
sample.seq has size >= 8
sample.entities have size >= 8		// You can use "have" in place of "has"
```

* Boolean Expressions

```scala

// Logical AND (not strictly required, since you can just split into separate rules)
( person.name is notEmpty ) and ( person.age should be >= 18 )

// Logical OR
( person.email is notEmpty ) or ( person.phoneNumber is notEmpty )

// You can also nest rules:
( fromJava.tags is aNull ) or (
  ( fromJava.tags is notEmpty ) and 
  ( fromJava.tags.each should matchRegexFully( "\\S+" ) )
)
```

# Custom Combinators

Before creating your own combinators, you should be well-acquainted with the [Accord API](api.html). You can define your own validation rules by simply extending `Validator[T]` and implementing `apply`; before you do, however, you should give some consideration as to the resulting violation. In most cases a simple `RuleViolation` is sufficient.

Accord provides three constructs to make defining new validators simpler:

* [`BaseValidator[T]`](https://github.com/wix/accord/blob/v{{ site.version.release }}/core/src/main/scala/com/wix/accord/BaseValidator.scala) lets you define a validator by providing a couple of functions: a *test* function determines whether or not a value is valid, and a *failure* function generates the corresponding [failure](api.html#result-model) otherwise.
* [`NullSafeValidator[T]`](https://github.com/wix/accord/blob/v{{ site.version.release }}/core/src/main/scala/com/wix/accord/BaseValidator.scala) extends `BaseValidator` by intercepting `null`s and treating them as failures. This allows you to keep your validation logic pure while avoiding `NullPointerException`s (especially useful for validating legacy Java classes).
* [`ViolationBuilder`](https://github.com/wix/accord/blob/v{{ site.version.release }}/core/src/main/scala/com/wix/accord/ViolationBuilder.scala) provides a set of convenient implicit conversions for generating violations.

The following example implements a simple validation rule that restricts the range of allowed values:

```scala
// With conveniences:
import com.wix.accord._
import ViolationBuilder._

def oneOf[ T <: AnyRef ]( options : T* ): Validator[ T ] = 
  new NullSafeValidator[ T ](
    test    = options.contains,
    failure = _ -> s"is not one of (${ options.mkString( "," ) })"
  )

case class Person( name: String, title: String ) 
implicit val personValidator = validator[ Person ] { p =>
  p.name is notEmpty
  p.title is oneOf( "Mr", "Mrs", "Miss" )
}
```

This is functionally equivalent to:

```scala
// Without conveniences:
import com.wix.accord._

def oneOf[ T ]( options : T* ): Validator[ T ] = 
  new Validator[ T ] {
    def apply( value: T ) = value match {
      case null => 
        Failure( Set( RuleViolation( null, "is a null", None ) ) )
      case _ if options contains value =>
        Success
      case _ =>
        Failure( Set( RuleViolation( value, "is not one of " + options.mkString( "," ), None ) ) )
    }
  }
```