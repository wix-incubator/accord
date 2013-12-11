[![Build Status](https://travis-ci.org/wix/accord.png)](https://travis-ci.org/holograph/accord)

Overview
========

Accord is a validation library written in and for Scala. Compared to [JSR 303](http://jcp.org/en/jsr/detail?id=303) and [Scalaz validation](https://github.com/scalaz/scalaz/blob/scalaz-seven/core/src/main/scala/scalaz/Validation.scala) it aims to provide the following:

* __Composable__: Because JSR 303 is annotation based, validation rules cannot be composed (annotations cannot receive other annotations as parameters). This is a real problem with some Scala features, for example `Option`s or collections. Accord's validation rules are trivially composable.
* __Simple__: Accord provides a dead-simple story for validation rule definition by leveraging macros, as well as the validation call site (see example below).
* __Self-contained__: Accord is macro-based but completely self-contained, and consequently only relies on the Scala runtime and reflection libraries.
* __Integrated__: Other than providing its own DSL and matcher library, Accord is intended to play well with [Hamcrest matchers](https://github.com/hamcrest/JavaHamcrest), and fully integrate with [Specs<sup>2</sup>](http://etorreborre.github.io/specs2/) and [ScalaTest](http://www.scalatest.org/).

Accord is developed and used at [![Wix.com Logo](wix_logo.png)](http://www.wix.com) and distributed under the [Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0), which basically means you can use and modify it freely. Feedback, bug reports and improvements are welcome!

Example
=======

Importing the library for use:

```scala
import com.wix.accord._
```

Defining a validator:

```scala
import dsl._    // Import the validator DSL

case class Person( firstName: String, lastName: String )
case class Classroom( teacher: Person, students: Seq[ Person ] )

implicit val personValidator = validator[ Person ] { p =>
  p.firstName is notEmpty                   // The expression being validated is resolved automatically, see below
  p.lastName as "last name" is notEmpty     // You can also explicitly describe the expression being validated
}

implicit val classValidator = validator[ Classroom ] { c =>
  c.teacher is valid        // Implicitly relies on personValidator!
  c.students.each is valid
  c.students have size > 0
}
```


Running a validator:

```
scala> val validPerson = Person( "Wernher", "von Braun" )
validPerson: Person = Person(Wernher,von Braun)

scala> validate( validPerson )
res0: com.wix.accord.Result = Success

scala> val invalidPerson = Person( "", "No First Name" )
invalidPerson: Person = Person(,No First Name)

scala> validate( invalidPerson )
res1: com.wix.accord.Result = Failure(List(RuleViolation(,must not be empty,firstName)))

scala> val explicitDescription = Person( "No Last Name", "" )
explicitDescription: Person = Person(No Last Name,)

scala> validate( explicitDescription )
res2: com.wix.accord.Result = Failure(List(RuleViolation(,must not be empty,last name)))

scala> val invalidClassroom = Classroom( Person( "Alfred", "Aho" ), Seq.empty )
invalidClassroom: Classroom = Classroom(Person(Alfred,Aho),List())

scala> validate( invalidClassroom )
res3: com.wix.accord.Result = Failure(List(RuleViolation(List(),has size 0, expected more than 0,students)))
```

Getting Started
===============

Accord is currently published to the Sonatype snapshot repository pending the 0.1-RELEASE milestone. Additionally, pending a fix for [issue #2](../../issues/2) it _requires_ Scala 2.10.3 or later.

SBT
---

To use Accord, add the Sonatype snapshot repository to your resolvers and add the dependency on Accord; typically this means adding the following to your `build.sbt` file:

```scala
resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"

libraryDependencies += "com.wix" %% "accord" % "0.1-SNAPSHOT"

scalaVersion := "2.10.3"
```

Maven
-----

To use Accord, add the Sonatype snapshot repository and a dependency on Accord to your POM:

```xml
<repositories>
  <repository>
    <id>sonatype-snapshots</id>
    <name>Sonatype snapshot repository</name>
    <url>https://oss.sonatype.org/content/repositories/snapshots</url>
    <releases><enabled>false</enabled></releases>
    <snapshots><enabled>true</enabled></snapshots>
  </repository>
</repositories>

<dependencies>
  <dependency>
    <groupId>com.wix</groupId>
    <artifactId>accord_${scala.tools.version}</artifactId>
    <version>0.1-SNAPSHOT</version>
  </dependency>
</dependencies>
```

DIY
---

Of course, you can always clone the repository and build it on your own:

```
git clone arilou:dev tomer$ git clone git@github.com:wix/accord.git
Cloning into 'accord'...
remote: Counting objects: 961, done.
remote: Compressing objects: 100% (332/332), done.
remote: Total 961 (delta 282), reused 910 (delta 231)
Receiving objects: 100% (961/961), 127.47 KiB | 138.00 KiB/s, done.
Resolving deltas: 100% (282/282), done.
Checking connectivity... done

arilou:dev tomer$ cd accord
arilou:accord tomer$ sbt package
[info] Loading global plugins from /Users/tomer/.sbt/0.13/plugins
[info] Loading project definition from /Users/tomer/dev/accord/project
[info] Updating {file:/Users/tomer/dev/accord/project/}accord-build...
[info] Resolving org.fusesource.jansi#jansi;1.4 ...
[info] Done updating.
[info] Set current project to root (in build file:/Users/tomer/dev/accord/)
[info] Updating {file:/Users/tomer/dev/accord/}api...
[info] Updating {file:/Users/tomer/dev/accord/}root...
[info] Resolving org.fusesource.jansi#jansi;1.4 ...
[info] Done updating.
[info] Resolving org.scala-lang#jline;2.10.3 ...
[info] Packaging /Users/tomer/dev/accord/target/scala-2.10/root_2.10-0.1-SNAPSHOT.jar ...
[info] Resolving org.fusesource.jansi#jansi;1.4 ...
[info] Done packaging.
[info] Done updating.
[info] Updating {file:/Users/tomer/dev/accord/}scalatest...
[info] Updating {file:/Users/tomer/dev/accord/}specs2...
[info] Resolving org.scala-lang#scala-library;2.10.3 ...
[info] Compiling 3 Scala sources to /Users/tomer/dev/accord/api/target/scala-2.10/classes...
[info] Resolving org.fusesource.jansi#jansi;1.4 ...
[info] Done updating.
[info] Resolving org.fusesource.jansi#jansi;1.4 ...
[info] Done updating.
[info] Updating {file:/Users/tomer/dev/accord/}core...
[info] Resolving org.fusesource.jansi#jansi;1.4 ...
[info] Done updating.
[info] Updating {file:/Users/tomer/dev/accord/}spring3...
[info] Resolving org.fusesource.jansi#jansi;1.4 ...
[info] Done updating.
[info] Packaging /Users/tomer/dev/accord/api/target/scala-2.10/accord-api_2.10-0.1-SNAPSHOT.jar ...
[info] Compiling 1 Scala source to /Users/tomer/dev/accord/specs2/target/scala-2.10/classes...
[info] Compiling 4 Scala sources to /Users/tomer/dev/accord/spring3/target/scala-2.10/classes...
[info] Compiling 7 Scala sources to /Users/tomer/dev/accord/core/target/scala-2.10/classes...
[info] Compiling 1 Scala source to /Users/tomer/dev/accord/scalatest/target/scala-2.10/classes...
[info] Done packaging.
[info] Packaging /Users/tomer/dev/accord/spring3/target/scala-2.10/accord-spring3_2.10-0.1-SNAPSHOT.jar ...
[info] Done packaging.
[info] Packaging /Users/tomer/dev/accord/specs2/target/scala-2.10/accord-specs2_2.10-0.1-SNAPSHOT.jar ...
[info] Done packaging.
[info] Packaging /Users/tomer/dev/accord/scalatest/target/scala-2.10/accord-scalatest_2.10-0.1-SNAPSHOT.jar ...
[info] Done packaging.
[info] Packaging /Users/tomer/dev/accord/core/target/scala-2.10/accord-core_2.10-0.1-SNAPSHOT.jar ...
[info] Done packaging.
[success] Total time: 18 s, completed Dec 11, 2013 10:10:33 AM
arilou:accord tomer$
```

Roadmap
=======

Accord is still fairly rudimentary, and there's plenty of improvements to be made:

* Major issues and improvements slated for the final release of 0.1:
    * ~~Publish snapshot (in preparation for release) on the central repository~~
    * ~~Rearchitect violation message infrastructure~~
    * ~~Implement accord-scalatest integration in a separate artifact~~
    * ~~Implement accord-specs2 integration in a separate artifact~~
    * ~~Support custom violation messages in the DSL (e.g. `p.firstName is notEmpty as "First name must not be empty!"`)~~
    * ~~Migrate to the Wix repository~~
    * Clean up and extend documentation
    * Release!
* Improvements under consideration for 0.2:
    * Additional combinators
    * Implement accord-hamcrest integration in a separate artifact
    * Support custom violation types (e.g. `p.firstName is notEmpty as MyServerError( code = -3 )`)

Ideas and feature requests welcome!

