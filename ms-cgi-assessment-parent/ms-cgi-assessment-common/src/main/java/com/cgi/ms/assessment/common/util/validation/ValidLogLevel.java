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
@Constraint(validatedBy = ValidIngredientValidator.class)
@Retention(RUNTIME)
@Documented
public @interface ValidLogLevel {

	String message() default "Log Level can't be empty and only TRACE | DEBUG | INFO | WARN | ERROR | FATAL are allowed log levels.";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};
	
}
