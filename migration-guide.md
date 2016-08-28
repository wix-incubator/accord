---
layout: page
title: "Migration Guide"
---

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

### Other Notes

With the experimental Scala 2.12 support, users must add a compile-time-only dependency on `scala-compiler`. With SBT this is as simple as:

```scala
libraryDependencies <+= 
  scalaVersion( "org.scala-lang" % "scala-compiler" % _ % "provided" )
```

With Maven this is similarly easy:

```xml
<dependencies>
  <dependency>
    <groupId>org.scala-lang</groupId>
    <artifactId>scala-compiler</artifactId>
    <version>${scala.version}</version>
  </dependency>
</dependencies>
```

