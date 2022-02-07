package com.cgi.ms.assessment.common.util.validation;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

@Target({ METHOD, FIELD, PARAMETER })
@Constraint(validatedBy = ValidLogTypeValidator.class)
@Retention(RUNTIME)
@Documented
public @interface ValidLogLevel {

	String message() default "{com.cgi.ms.web.log.valid.log.type.value}";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};
	
}
