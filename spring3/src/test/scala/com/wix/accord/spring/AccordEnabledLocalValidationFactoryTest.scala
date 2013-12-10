package com.wix.accord.spring

import org.scalatest.{WordSpec, Matchers}
import org.springframework.test.context.{TestContextManager, ContextConfiguration}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.validation.{BeanPropertyBindingResult, Validator}

import AccordEnabledLocalValidationFactoryTest._

@ContextConfiguration( classes = Array( classOf[ SpringValidationConfiguration ] ) )
class AccordEnabledLocalValidationFactoryTest extends WordSpec with Matchers {

  new TestContextManager( this.getClass ).prepareTestInstance( this )

  @Autowired var validator: Validator = _

  "The validation factory" should {
    "find and apply an Accord validator in a companion object" in {
      val test = AccordEnabled( "ok", 15 )
      val errors = new BeanPropertyBindingResult( test, test.getClass.getSimpleName )
      validator.validate( test, errors )
      errors.hasErrors shouldBe true
    }
    "fall back to JSR 303 validation if an Accord validator is unavailable" in {
      val test = JSR303Enabled( "most certainly not ok", 15 )
      val errors = new BeanPropertyBindingResult( test, test.getClass.getSimpleName )
      validator.validate( test, errors )
      errors.hasErrors shouldBe true
    }
  }

}

object AccordEnabledLocalValidationFactoryTest {
  import javax.validation.constraints.{Max, Size, NotNull}
  import scala.annotation.meta.field

  case class JSR303Enabled( @( NotNull @field ) @( Size @field )( max = 10 ) f1: String,
                            @( Max @field )( value = 10 ) f2: Int )
  case class AccordEnabled( f1: String, f2: Int )
  case object AccordEnabled {
    import com.wix.accord.dsl._
    implicit val accordEnabledValidator = validator[ AccordEnabled ] { ae =>
      ae.f1 has size <= 10
      ae.f2 should be <= 10
    }
  }

  import org.springframework.context.annotation.{Configuration, Bean}

  @Configuration
  class SpringValidationConfiguration {
    @Bean def resolver: AccordValidatorResolver = new CompanionObjectAccordValidatorResolver
    @Bean def validator = new AccordEnabledLocalValidationFactory
  }
}
