---
layout: page
title: "Defining Validators"
---

# Overview

Accord provides a convenient DSL for defining validation rules. To define a validator over some type `T`, import the `com.wix.accord.dsl` package, and invoke the `validator[T]` function (where `T` is your specific type under validation). You can then use the provided sample object to define various rules:

```scala

class MyClass { /* ... */ }

import com.wix.accord.dsl._    // Import the validator DSL


val myClassValidator = validator[ MyClass ] { s =>
	// Rules
}
```

Accord adds an implicit logical `and` relation between the rules, so all rules must apply in order for the validation to be successful. You can specify as many rules as you like.

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

# Extending Accord

Before creating your own combinators, you should be well-acquainted with the [Accord API](api.html). You can define your own validation rules by simply extending `Validator[T]` and implementing `apply`; before you do, however, you should give some consideration as to the resulting violation. 
