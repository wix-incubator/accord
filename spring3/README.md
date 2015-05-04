# Getting started

Accord provides an integration layer for [Spring Validation](http://docs.spring.io/spring/docs/current/spring-framework-reference/html/validation.html) as part of the `accord-spring3` module. The first step is to add the dependency to your build configuration:

## Using sbt

```scala
libraryDependencies += "com.wix" %% "accord-spring3" % "0.4.2"
```

## Using Maven

Add the following to your POM:

```xml
<dependency>
    <groupId>com.wix</groupId>
    <artifactId>accord-spring3_${scala.tools.version}</artifactId>
    <version>0.4.2</version>
</dependency>
```

# Hookup into Spring

Two elements are required to integrate Accord with Spring:

## Validator Resolver

Because Accord is intended to be used in Scala code directly, it does not provide any out-of-the-box facilities for registering validators with an IoC container. The Spring integration layer adds the concept of a _validator resolver_, which is essentially responsible for answering the question: given a type `T`, what is `Validator[T]`? This is normally handled by the implicit parameter requirement of the Accord `validate` API.

A resolver extends the trait `AccordValidatorResolver`; currently the only implementation available out-of-the-box is the default `CompanionObjectAccordValidatorResolver`, which for any class `T` tries to find a suitable validator in the companion object for `T`. For example:

```scala
object ExampleModel {
  import dsl._

  case class Test1( field: Int )
  case object Test1 {
    implicit val validatorOfTest1 = validator[ Test1 ] { t => t.field should be > 5 }
  }

  case class Test2( field: String )
  implicit val validatorOfTest2 = validator[ Test2 ] { t => t.field is notEmpty }
}
```

In this case, the `Test1` validator will be correctly resolved. but the `Test2` validator will not be found because it does not reside in the `Test2` companion object.

## Validator Factory

Spring Validation [mandates](http://docs.spring.io/spring/docs/current/spring-framework-reference/html/validation.html#validation-beanvalidation-spring) the use of a `LocalValidatorFactoryBean` to bootstrap the JSR 303 validation engine and handle registration within Spring. Accord provides a natural migration path by wrapping this factory with its own `AccordEnabledLocalValidationFactory`. For any given type, it first looks up an Accord validator (via a provided resolver); if available, this validator is then used. If not, it falls back to the normal Spring implementation, which enables you to add or switch to Accord piecemeal without sacrificing your existing validation code.

In practice, to enable Spring integration for Accord, you only need to register two beans: your resolver of choice and the validator factory. If you already have a validator factory bean set up per the Spring Validation documentation, you only have to change the `org.springframework.validation.beanvalidation.LocalValidatorFactoryBean` bean class to `com.wix.accord.spring.AccordEnabledLocalValidationFactory` and you're good to go, as in this example:

```xml
<bean id="accordValidatorResolver" class="com.wix.accord.spring.CompanionObjectAccordValidatorResolver" />
<bean id="validator" class="com.wix.accord.spring.AccordEnabledLocalValidationFactory" />
```

# Caveats

The Spring integration facilities are somewhat limited, and there are a number of caveats that you should be aware of:

* Because Accord's violation model doesn't assume a common layout for objects under validation, and furthermore allows a validator to provide an explicit description for any expression, there's no easy way to map the resulting violations to the Spring Validation [`Errors`](http://docs.spring.io/spring/docs/3.2.x/javadoc-api/index.html?org/springframework/validation/Errors.html) API. As a result, all Accord violations are recorded as `ObjectError`s and consequently do not provide for nesting. It's possible that subset of Accord violations could be mapped to the Bean-oriented `FieldError` API, but we currently consider this a low priority.
* For the same reason, all Accord violations are registered with a single error code `accord.validation.failure`, and instead generate a `defaultMessage` for each specific violation to accurately describe it.
* Validator lookup is not memoized, which could entail a performance impact. Caching may be added in the future if this turns out to be a problem in practice.

