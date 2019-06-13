package com.wix.accord.spring;

class AccordValidatorAdapterTest extends WordSpec with Matchers {

  "The validation adapter" should {
    def adapter = new AccordValidatorAdapter( testClassValidator )

    "find and apply an Accord validator in a companion object" in {
      val test = TestClass( "ok", 5 )
      val errors: Errors = new BeanPropertyBindingResult( test, "test" )
      adapter.validate( test, errors )

      errors.getAllErrors shouldBe empty
    }

    "correctly render a validation error" in {
      val test = TestClass( "not ok", 15 )
      val errors: Errors = new BeanPropertyBindingResult( test, "test" )
      adapter.validate( test, errors )

      errors.getAllErrors should have size 1
      errors.getAllErrors.asScala.head.getCode shouldEqual SpringAdapterBase.defaultErrorCode
      errors.getAllErrors.asScala.head.getDefaultMessage shouldEqual "f2 got 15, expected 10 or less"
    }

    "correctly render multiple validation errors" in {
      val test = TestClass( "not ok at all", 15 )
      val expectedErrorMessages = Seq(
        "f1 has size 13, expected 10 or less",
        "f2 got 15, expected 10 or less"
      )

      val errors: Errors = new BeanPropertyBindingResult( test, "test" )
      adapter.validate( test, errors )
      errors.getAllErrors should have size 2
      val returnedMessages = errors.getAllErrors.asScala map { _.getDefaultMessage }
      returnedMessages should contain only( expectedErrorMessages:_* )
    }
  }

}
