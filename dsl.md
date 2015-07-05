---
layout: page
title: "Defining Validators"
---

# Overview

Accord provides a convenient DSL for defining validation rules. To define a validator over some type `T`, import the `com.wix.accord.dsl` package, and invoke the `validator[T]` function (where `T` is your specific type under validation). You can then use the provided sample object to define various rules:

```scala
import com.wix.accord.dsl._    // Import the validator DSL

class MyClass { /* ... */ }

val myClassValidator = validator[ MyClass ] { s =>
	// Rules
}
```

Accord adds an implicit logical `and` relation between the rules, so all rules must apply in order for the validation to be successful. You can specify as many rules as you like.


# Combinators

Accord offers a built-in library of building blocks (called "combinators") that can be composed into more complex validation rules.

## General-purpose

```scala
// Equality
sample.field is equalTo( "value" )
sample.field is notEqualTo( "value" )

// Nullability (only applies to reference types)
sample.field is aNull
sample.field is notNull

// Delegation
sample.field is valid    					// Implicitly, or
sample.field is valid( myOwnValidator )		// Explicitly
```

## Primitives

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

## Collections

```scala

// Emptiness (applies to any type that has a boolean "isEmpty" property, such as string)
sample.seqField is empty
sample.seqField is notEmpty

// Size (applies to any type with an integer "size" property, such as string). All numeric operations apply:
sample.seqField.size should be >= 8
```

## Boolean Expressions


And
Or

