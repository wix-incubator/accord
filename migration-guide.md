---
layout: page
title: "Migration Guide"
---

Migrating to 0.7
================

The description framework saw a significant revamp with 0.7 as a result of community feedback (shout out to the Marathon team for a really serious use case!). Instead of one, hard to interact with `Description` trait, Accord now features the `Path` type (really a sequence of `Description` elements), as well as a cleaned-up description class hierarchy.

In practical terms, this means:

* Single instances of `Description` should not be used as full paths. An implicit conversion is provided for backwards-compatibility, is marked deprecated and is expected to go away in the next release of Accord.
* The `description` property of all `Violation`s is no longer available; you should use the `path` property instead.
* The `description` property of `RuleViolationMatcher` and `GroupViolationMatcher` is no longer available; you should use the `path` property instead.
* `AccessChain` is deprecated; you should use `Path` instead. For example, `AccessChain( Generic( "property" ) )` now becomes `Path( Generic( "property" ) )`. A companion is provided for backwards-compatibility, is marked deprecated and is expected to go away in the next release of Accord.
* `SelfReference` is deprecated, and is now represented by `Path.empty`. The symbol itself is made available for backwards compatibility, is marked deprecated and is expected to go away in the next release of Accord.
* `Conditional` is deprecated. You should use `Branch` (for `if`s) or `PatternMatch` as appropriate. A convenience 

Migrating to 0.6
================

### Descriptions

Release 0.6 introduces several new features, the most prominent of which is the new description framework. Take the following simple validator:

```scala
case class Person( name: String, age: Int )

implicit val personValidator = validator[ Person ] { p =>
  p.name is notEmpty
  p.age should be >= 0
}
```

Every validation rule has its own target (internally called _object under validation_), and Accord automagically generates descriptions for each rule, which are subsequently included in validation results when a rule is violated.

Prior to 0.6, Accord produced string descriptions; for example, the above rules would produce the descriptions "name" and "age" respectively. While easy to use, a more precise representation was necessary for both internal development (e.g. supporting array indexing) and external features (e.g. better error reporting). Consequently, the API has been extended to include a full-blown [description model](api.html#description-model), and a String-rendering function `Descriptions.render` that largely mimics the old behavior.

In effect, this will only affect you if you're actually processing descriptions. To simplify migration, both [Specs²](specs2.html) or [ScalaTest](scalatest.html) `ResultMatchers` traits provide backwards-compatible APIs so your tests will not break (with one exception, see below). Be advised, however, that these are deprecated and slated to be removed in a future version of Accord, so heed your compiler warnings!

Finally, if you're actually instantiating matchers on your own and wish to use old-style string descriptions, you can use the `legacyDescription` property that mimics the old behavior:

```scala
// Before 0.6:
validate( classroom ) should failWith( GroupViolationMatcher( description = "students" ) )

// After 0.6:
validate( classroom ) should failWith( GroupViolationMatcher( legacyDescription = "students" ) )
```

### Specs²

As of 0.6, Accord drops support for the older Specs² 2.x series. While the API is compatible enough, the amount of work required to maintain the build matrix was simply not worthwhile. As a result, the module name has been simplified simply to `accord-specs2`:

```scala
// Before 0.6 (with 3.x):
libraryDependencies += "com.wix" %% "accord-specs2-3-x" % "0.5" % "test"

// After 0.6:
libraryDependencies += "com.wix" %% "accord-specs2" % "0.6" % "test"
```
